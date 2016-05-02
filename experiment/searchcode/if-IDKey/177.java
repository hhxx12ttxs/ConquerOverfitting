/**
 *  Copyright (c) 2008, 2009 Florian Pirchner (Vienna, Austria) and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  Contributors:
 *          Ekkehard Gentz     - ideas, tests and requirements 
 *          Florian Pirchner   - ideas, initial API and implementation
 *     
 *   more info: http://redview.org
 */
package org.redview.model.datatypes.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.redview.model.binding.BindingPackage;

import org.redview.model.binding.impl.BindingPackageImpl;

import org.redview.model.converter.ConverterPackage;

import org.redview.model.converter.impl.ConverterPackageImpl;

import org.redview.model.datatypes.DatatypesFactory;
import org.redview.model.datatypes.DatatypesPackage;
import org.redview.model.datatypes.MDatatype;
import org.redview.model.datatypes.MIdGeneratorInfo;
import org.redview.model.datatypes.MModelInsertStrategy;
import org.redview.model.datatypes.MModeledElement;
import org.redview.model.datatypes.MModellingCategory;
import org.redview.model.datatypes.MTransientModellingInfo;
import org.redview.model.datatypes.Ri18nFormat;
import org.redview.model.datatypes.Ri18nKey;
import org.redview.model.datatypes.Ri18nTooltipKey;

import org.redview.model.dnd.DndPackage;

import org.redview.model.dnd.impl.DndPackageImpl;

import org.redview.model.elements.ElementsPackage;

import org.redview.model.elements.impl.ElementsPackageImpl;

import org.redview.model.events.EventsPackage;

import org.redview.model.events.eventsimpl.EventsimplPackage;

import org.redview.model.events.eventsimpl.impl.EventsimplPackageImpl;

import org.redview.model.events.impl.EventsPackageImpl;

import org.redview.model.events.triggerimpl.TriggerimplPackage;

import org.redview.model.events.triggerimpl.impl.TriggerimplPackageImpl;

import org.redview.model.layout.LayoutPackage;

import org.redview.model.layout.impl.LayoutPackageImpl;

import org.redview.model.layout.layoutData.LayoutDataPackage;

import org.redview.model.layout.layoutData.impl.LayoutDataPackageImpl;

import org.redview.model.styles.StylesPackage;

import org.redview.model.styles.impl.StylesPackageImpl;

import org.redview.model.validation.ValidationPackage;

import org.redview.model.validation.impl.ValidationPackageImpl;

import org.redview.model.visibility.VisibilityPackage;

import org.redview.model.visibility.impl.VisibilityPackageImpl;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Package</b>. <!--
 * end-user-doc -->
 * @generated
 */
public class DatatypesPackageImpl extends EPackageImpl implements
		DatatypesPackage {
	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mModeledElementEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mTransientModellingInfoEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ri18nKeyEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ri18nFormatEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ri18nTooltipKeyEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mIdGeneratorInfoEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mDatatypeEClass = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum mModellingCategoryEEnum = null;

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum mModelInsertStrategyEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the
	 * package package URI value.
	 * <p>
	 * Note: the correct way to create the package is via the static factory
	 * method {@link #init init()}, which also performs initialization of the
	 * package, or returns the registered package, if one already exists. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.redview.model.datatypes.DatatypesPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private DatatypesPackageImpl() {
		super(eNS_URI, DatatypesFactory.eINSTANCE);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model,
	 * and for any others upon which it depends.
	 * 
	 * <p>
	 * This method is used to initialize {@link DatatypesPackage#eINSTANCE} when
	 * that field is accessed. Clients should not invoke it directly. Instead,
	 * they should simply access that field to obtain the package. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static DatatypesPackage init() {
		if (isInited) return (DatatypesPackage)EPackage.Registry.INSTANCE.getEPackage(DatatypesPackage.eNS_URI);

		// Obtain or create and register package
		DatatypesPackageImpl theDatatypesPackage = (DatatypesPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof DatatypesPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new DatatypesPackageImpl());

		isInited = true;

		// Obtain or create and register interdependencies
		ElementsPackageImpl theElementsPackage = (ElementsPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ElementsPackage.eNS_URI) instanceof ElementsPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ElementsPackage.eNS_URI) : ElementsPackage.eINSTANCE);
		StylesPackageImpl theStylesPackage = (StylesPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(StylesPackage.eNS_URI) instanceof StylesPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(StylesPackage.eNS_URI) : StylesPackage.eINSTANCE);
		EventsPackageImpl theEventsPackage = (EventsPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(EventsPackage.eNS_URI) instanceof EventsPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(EventsPackage.eNS_URI) : EventsPackage.eINSTANCE);
		EventsimplPackageImpl theEventsimplPackage = (EventsimplPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(EventsimplPackage.eNS_URI) instanceof EventsimplPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(EventsimplPackage.eNS_URI) : EventsimplPackage.eINSTANCE);
		TriggerimplPackageImpl theTriggerimplPackage = (TriggerimplPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(TriggerimplPackage.eNS_URI) instanceof TriggerimplPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(TriggerimplPackage.eNS_URI) : TriggerimplPackage.eINSTANCE);
		VisibilityPackageImpl theVisibilityPackage = (VisibilityPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(VisibilityPackage.eNS_URI) instanceof VisibilityPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(VisibilityPackage.eNS_URI) : VisibilityPackage.eINSTANCE);
		BindingPackageImpl theBindingPackage = (BindingPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(BindingPackage.eNS_URI) instanceof BindingPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(BindingPackage.eNS_URI) : BindingPackage.eINSTANCE);
		DndPackageImpl theDndPackage = (DndPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(DndPackage.eNS_URI) instanceof DndPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(DndPackage.eNS_URI) : DndPackage.eINSTANCE);
		ValidationPackageImpl theValidationPackage = (ValidationPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ValidationPackage.eNS_URI) instanceof ValidationPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ValidationPackage.eNS_URI) : ValidationPackage.eINSTANCE);
		ConverterPackageImpl theConverterPackage = (ConverterPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(ConverterPackage.eNS_URI) instanceof ConverterPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(ConverterPackage.eNS_URI) : ConverterPackage.eINSTANCE);
		LayoutPackageImpl theLayoutPackage = (LayoutPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(LayoutPackage.eNS_URI) instanceof LayoutPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(LayoutPackage.eNS_URI) : LayoutPackage.eINSTANCE);
		LayoutDataPackageImpl theLayoutDataPackage = (LayoutDataPackageImpl)(EPackage.Registry.INSTANCE.getEPackage(LayoutDataPackage.eNS_URI) instanceof LayoutDataPackageImpl ? EPackage.Registry.INSTANCE.getEPackage(LayoutDataPackage.eNS_URI) : LayoutDataPackage.eINSTANCE);

		// Create package meta-data objects
		theDatatypesPackage.createPackageContents();
		theElementsPackage.createPackageContents();
		theStylesPackage.createPackageContents();
		theEventsPackage.createPackageContents();
		theEventsimplPackage.createPackageContents();
		theTriggerimplPackage.createPackageContents();
		theVisibilityPackage.createPackageContents();
		theBindingPackage.createPackageContents();
		theDndPackage.createPackageContents();
		theValidationPackage.createPackageContents();
		theConverterPackage.createPackageContents();
		theLayoutPackage.createPackageContents();
		theLayoutDataPackage.createPackageContents();

		// Initialize created meta-data
		theDatatypesPackage.initializePackageContents();
		theElementsPackage.initializePackageContents();
		theStylesPackage.initializePackageContents();
		theEventsPackage.initializePackageContents();
		theEventsimplPackage.initializePackageContents();
		theTriggerimplPackage.initializePackageContents();
		theVisibilityPackage.initializePackageContents();
		theBindingPackage.initializePackageContents();
		theDndPackage.initializePackageContents();
		theValidationPackage.initializePackageContents();
		theConverterPackage.initializePackageContents();
		theLayoutPackage.initializePackageContents();
		theLayoutDataPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theDatatypesPackage.freeze();

  
		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(DatatypesPackage.eNS_URI, theDatatypesPackage);
		return theDatatypesPackage;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMModeledElement() {
		return mModeledElementEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_Reference1() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_Reference2() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_RootAttribute() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_RootType() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_ModelAssociation() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_ModelClassName() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_LeafAttribute() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_NodeTypePath() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_NodeAttributePath() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_ModelInfo() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_HideAbstract() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_LeafType() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_AttributePath() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(12);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_ModelSuperClass() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(13);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_AbstractAssociationClass() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(14);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_AbstractAssociation() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(15);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMModeledElement_ModelEnum() {
		return (EAttribute)mModeledElementEClass.getEStructuralFeatures().get(16);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMTransientModellingInfo() {
		return mTransientModellingInfoEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMTransientModellingInfo_Transformed() {
		return (EAttribute)mTransientModellingInfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMTransientModellingInfo_Category() {
		return (EAttribute)mTransientModellingInfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRi18nKey() {
		return ri18nKeyEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRi18nKey_I18nLabelkey() {
		return (EAttribute)ri18nKeyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRi18nFormat() {
		return ri18nFormatEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRi18nFormat_I18nFormat() {
		return (EAttribute)ri18nFormatEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getRi18nTooltipKey() {
		return ri18nTooltipKeyEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getRi18nTooltipKey_I18nTooltipkey() {
		return (EAttribute)ri18nTooltipKeyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMIdGeneratorInfo() {
		return mIdGeneratorInfoEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMIdGeneratorInfo_IdKey() {
		return (EAttribute)mIdGeneratorInfoEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMIdGeneratorInfo_IdCounter() {
		return (EAttribute)mIdGeneratorInfoEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMIdGeneratorInfo_IdConstant() {
		return (EAttribute)mIdGeneratorInfoEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EAttribute getMIdGeneratorInfo_IdValue() {
		return (EAttribute)mIdGeneratorInfoEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EClass getMDatatype() {
		return mDatatypeEClass;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getMModellingCategory() {
		return mModellingCategoryEEnum;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public EEnum getMModelInsertStrategy() {
		return mModelInsertStrategyEEnum;
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	public DatatypesFactory getDatatypesFactory() {
		return (DatatypesFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		mModeledElementEClass = createEClass(MMODELED_ELEMENT);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__REFERENCE1);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__REFERENCE2);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__ROOT_ATTRIBUTE);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__ROOT_TYPE);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__MODEL_ASSOCIATION);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__MODEL_CLASS_NAME);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__LEAF_ATTRIBUTE);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__NODE_TYPE_PATH);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__NODE_ATTRIBUTE_PATH);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__MODEL_INFO);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__HIDE_ABSTRACT);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__LEAF_TYPE);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__ATTRIBUTE_PATH);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__MODEL_SUPER_CLASS);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__ABSTRACT_ASSOCIATION_CLASS);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__ABSTRACT_ASSOCIATION);
		createEAttribute(mModeledElementEClass, MMODELED_ELEMENT__MODEL_ENUM);

		mTransientModellingInfoEClass = createEClass(MTRANSIENT_MODELLING_INFO);
		createEAttribute(mTransientModellingInfoEClass, MTRANSIENT_MODELLING_INFO__TRANSFORMED);
		createEAttribute(mTransientModellingInfoEClass, MTRANSIENT_MODELLING_INFO__CATEGORY);

		ri18nKeyEClass = createEClass(RI18N_KEY);
		createEAttribute(ri18nKeyEClass, RI18N_KEY__I18N_LABELKEY);

		ri18nFormatEClass = createEClass(RI18N_FORMAT);
		createEAttribute(ri18nFormatEClass, RI18N_FORMAT__I18N_FORMAT);

		ri18nTooltipKeyEClass = createEClass(RI18N_TOOLTIP_KEY);
		createEAttribute(ri18nTooltipKeyEClass, RI18N_TOOLTIP_KEY__I18N_TOOLTIPKEY);

		mIdGeneratorInfoEClass = createEClass(MID_GENERATOR_INFO);
		createEAttribute(mIdGeneratorInfoEClass, MID_GENERATOR_INFO__ID_KEY);
		createEAttribute(mIdGeneratorInfoEClass, MID_GENERATOR_INFO__ID_COUNTER);
		createEAttribute(mIdGeneratorInfoEClass, MID_GENERATOR_INFO__ID_CONSTANT);
		createEAttribute(mIdGeneratorInfoEClass, MID_GENERATOR_INFO__ID_VALUE);

		mDatatypeEClass = createEClass(MDATATYPE);

		// Create enums
		mModellingCategoryEEnum = createEEnum(MMODELLING_CATEGORY);
		mModelInsertStrategyEEnum = createEEnum(MMODEL_INSERT_STRATEGY);
	}

	/**
	 * <!-- begin-user-doc --> <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model. This
	 * method is guarded to have no affect on any invocation but its first. <!--
	 * begin-user-doc --> <!-- end-user-doc -->
	 * 
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes and features; add operations and parameters
		initEClass(mModeledElementEClass, MModeledElement.class, "MModeledElement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMModeledElement_Reference1(), ecorePackage.getEString(), "reference1", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_Reference2(), ecorePackage.getEString(), "reference2", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_RootAttribute(), ecorePackage.getEString(), "rootAttribute", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_RootType(), ecorePackage.getEString(), "rootType", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_ModelAssociation(), ecorePackage.getEString(), "modelAssociation", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_ModelClassName(), ecorePackage.getEString(), "modelClassName", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_LeafAttribute(), ecorePackage.getEString(), "leafAttribute", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_NodeTypePath(), ecorePackage.getEString(), "nodeTypePath", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_NodeAttributePath(), ecorePackage.getEString(), "nodeAttributePath", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_ModelInfo(), ecorePackage.getEString(), "modelInfo", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_HideAbstract(), ecorePackage.getEBoolean(), "hideAbstract", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_LeafType(), ecorePackage.getEString(), "leafType", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_AttributePath(), ecorePackage.getEString(), "attributePath", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_ModelSuperClass(), ecorePackage.getEString(), "modelSuperClass", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_AbstractAssociationClass(), ecorePackage.getEString(), "abstractAssociationClass", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_AbstractAssociation(), ecorePackage.getEBoolean(), "abstractAssociation", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMModeledElement_ModelEnum(), ecorePackage.getEString(), "modelEnum", null, 0, 1, MModeledElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mTransientModellingInfoEClass, MTransientModellingInfo.class, "MTransientModellingInfo", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMTransientModellingInfo_Transformed(), ecorePackage.getEBoolean(), "transformed", null, 0, 1, MTransientModellingInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMTransientModellingInfo_Category(), this.getMModellingCategory(), "category", null, 0, 1, MTransientModellingInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ri18nKeyEClass, Ri18nKey.class, "Ri18nKey", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRi18nKey_I18nLabelkey(), ecorePackage.getEString(), "i18nLabelkey", null, 0, 1, Ri18nKey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ri18nFormatEClass, Ri18nFormat.class, "Ri18nFormat", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRi18nFormat_I18nFormat(), ecorePackage.getEString(), "i18nFormat", null, 0, 1, Ri18nFormat.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ri18nTooltipKeyEClass, Ri18nTooltipKey.class, "Ri18nTooltipKey", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRi18nTooltipKey_I18nTooltipkey(), ecorePackage.getEString(), "i18nTooltipkey", null, 0, 1, Ri18nTooltipKey.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mIdGeneratorInfoEClass, MIdGeneratorInfo.class, "MIdGeneratorInfo", IS_ABSTRACT, IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMIdGeneratorInfo_IdKey(), ecorePackage.getEString(), "idKey", null, 0, 1, MIdGeneratorInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMIdGeneratorInfo_IdCounter(), ecorePackage.getEInt(), "idCounter", null, 0, 1, MIdGeneratorInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMIdGeneratorInfo_IdConstant(), ecorePackage.getEString(), "idConstant", null, 0, 1, MIdGeneratorInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMIdGeneratorInfo_IdValue(), ecorePackage.getELong(), "idValue", null, 0, 1, MIdGeneratorInfo.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mDatatypeEClass, MDatatype.class, "MDatatype", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		// Initialize enums and add enum literals
		initEEnum(mModellingCategoryEEnum, MModellingCategory.class, "MModellingCategory");
		addEEnumLiteral(mModellingCategoryEEnum, MModellingCategory.EMBEDDABLE);
		addEEnumLiteral(mModellingCategoryEEnum, MModellingCategory.GENERAL);
		addEEnumLiteral(mModellingCategoryEEnum, MModellingCategory.ENTITY);
		addEEnumLiteral(mModellingCategoryEEnum, MModellingCategory.ONE_TO_ONE);
		addEEnumLiteral(mModellingCategoryEEnum, MModellingCategory.ONE_TO_MANY);
		addEEnumLiteral(mModellingCategoryEEnum, MModellingCategory.MANY_TO_ONE);
		addEEnumLiteral(mModellingCategoryEEnum, MModellingCategory.MANY_TO_MANY);

		initEEnum(mModelInsertStrategyEEnum, MModelInsertStrategy.class, "MModelInsertStrategy");
		addEEnumLiteral(mModelInsertStrategyEEnum, MModelInsertStrategy.LABEL);
		addEEnumLiteral(mModelInsertStrategyEEnum, MModelInsertStrategy.LABELED_ENTRY);
		addEEnumLiteral(mModelInsertStrategyEEnum, MModelInsertStrategy.GROUP);
		addEEnumLiteral(mModelInsertStrategyEEnum, MModelInsertStrategy.COMPOSITE);
		addEEnumLiteral(mModelInsertStrategyEEnum, MModelInsertStrategy.ELEMENTS);
		addEEnumLiteral(mModelInsertStrategyEEnum, MModelInsertStrategy.TAB_FOLDER);

		// Create resource
		createResource(eNS_URI);
	}

} // DatatypesPackageImpl

