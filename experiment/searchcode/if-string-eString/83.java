/**
 * Generated with Acceleo
 */
package org.obeonetwork.dsl.database.components;

// Start of user code for imports
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.eef.runtime.api.notify.EStructuralFeatureNotificationFilter;
import org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent;
import org.eclipse.emf.eef.runtime.api.notify.NotificationFilter;
import org.eclipse.emf.eef.runtime.context.PropertiesEditingContext;
import org.eclipse.emf.eef.runtime.context.impl.EObjectPropertiesEditionContext;
import org.eclipse.emf.eef.runtime.impl.components.SinglePartPropertiesEditingComponent;
import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
import org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart;
import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
import org.eclipse.emf.eef.runtime.impl.utils.EEFUtils;
import org.eclipse.emf.eef.runtime.policies.PropertiesEditingPolicy;
import org.eclipse.emf.eef.runtime.providers.PropertiesEditingProvider;
import org.eclipse.emf.eef.runtime.ui.widgets.ButtonsModeEnum;
import org.eclipse.emf.eef.runtime.ui.widgets.eobjflatcombo.EObjectFlatComboSettings;
import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettingsBuilder;
import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettingsBuilder.EEFEditorSettingsImpl;
import org.eclipse.emf.eef.runtime.ui.widgets.settings.NavigationStepBuilder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.obeonetwork.dsl.database.Column;
import org.obeonetwork.dsl.database.DatabaseFactory;
import org.obeonetwork.dsl.database.DatabasePackage;
import org.obeonetwork.dsl.database.Sequence;
import org.obeonetwork.dsl.database.parts.ColumnPropertiesEditionPart;
import org.obeonetwork.dsl.database.parts.DatabaseViewsRepository;
import org.obeonetwork.dsl.typeslibrary.NativeType;
import org.obeonetwork.dsl.typeslibrary.TypesLibraryPackage;

// End of user code

/**
 * 
 * 
 */
public class ColumnPropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String COLUMN_PART = "Column"; //$NON-NLS-1$

	/**
	 * Settings for sequence EObjectFlatComboViewer
	 */
	private EObjectFlatComboSettings sequenceSettings;

	/**
	 * Settings for type editor
	 */
	protected EEFEditorSettingsImpl typeSettings = (EEFEditorSettingsImpl) EEFEditorSettingsBuilder
			.create(this.semanticObject,
					TypesLibraryPackage.eINSTANCE.getTypeInstance_NativeType())
			.nextStep(
					NavigationStepBuilder
							.create(DatabasePackage.eINSTANCE.getColumn_Type())
							.index(0)
							.discriminator(
									TypesLibraryPackage.Literals.TYPE_INSTANCE)
							.build()).build();

	/**
	 * Settings for length editor
	 */
	protected EEFEditorSettingsImpl lengthSettings = (EEFEditorSettingsImpl) EEFEditorSettingsBuilder
			.create(this.semanticObject,
					TypesLibraryPackage.eINSTANCE.getTypeInstance_Length())
			.nextStep(
					NavigationStepBuilder
							.create(DatabasePackage.eINSTANCE.getColumn_Type())
							.index(0)
							.discriminator(
									TypesLibraryPackage.Literals.TYPE_INSTANCE)
							.build()).build();

	/**
	 * Settings for precision editor
	 */
	protected EEFEditorSettingsImpl precisionSettings = (EEFEditorSettingsImpl) EEFEditorSettingsBuilder
			.create(this.semanticObject,
					TypesLibraryPackage.eINSTANCE.getTypeInstance_Precision())
			.nextStep(
					NavigationStepBuilder
							.create(DatabasePackage.eINSTANCE.getColumn_Type())
							.index(0)
							.discriminator(
									TypesLibraryPackage.Literals.TYPE_INSTANCE)
							.build()).build();

	/**
	 * Settings for literals editor
	 */
	protected EEFEditorSettingsImpl literalsSettings = (EEFEditorSettingsImpl) EEFEditorSettingsBuilder
			.create(this.semanticObject,
					TypesLibraryPackage.eINSTANCE.getTypeInstance_Literals())
			.nextStep(
					NavigationStepBuilder
							.create(DatabasePackage.eINSTANCE.getColumn_Type())
							.index(0)
							.discriminator(
									TypesLibraryPackage.Literals.TYPE_INSTANCE)
							.build()).build();

	/**
	 * Default constructor
	 * 
	 */
	public ColumnPropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject column, final String editing_mode) {
		super(editingContext, column, editing_mode);
		this.parts = new String[] { COLUMN_PART };
		this.repositoryKey = DatabaseViewsRepository.class;
		this.partKey = DatabaseViewsRepository.Column.class;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#initPart(java.lang.Object,
	 *      int, org.eclipse.emf.ecore.EObject,
	 *      org.eclipse.emf.ecore.resource.ResourceSet)
	 * 
	 */
	@Override
	public void initPart(final Object key, final int kind, final EObject elt,
			final ResourceSet allResource) {
		this.setInitializing(true);
		if ((this.editingPart != null) && (key == this.partKey)) {
			this.editingPart.setContext(elt, allResource);
			if (this.editingPart instanceof CompositePropertiesEditionPart) {
				((CompositePropertiesEditionPart) this.editingPart)
						.getSettings().add(this.typeSettings);
				((CompositePropertiesEditionPart) this.editingPart)
						.getSettings().add(this.lengthSettings);
				((CompositePropertiesEditionPart) this.editingPart)
						.getSettings().add(this.precisionSettings);
				((CompositePropertiesEditionPart) this.editingPart)
						.getSettings().add(this.literalsSettings);
			}
			final Column column = (Column) elt;
			final ColumnPropertiesEditionPart columnPart = (ColumnPropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.name)) {
				columnPart.setName(EEFConverterUtil.convertToString(
						EcorePackage.Literals.ESTRING, column.getName()));
			}

			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.nullable)) {
				columnPart.setNullable(column.isNullable());
			}
			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.primaryKey)) {
				columnPart.setPrimaryKey(column.isInPrimaryKey());
			}
			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.unique)) {
				columnPart.setUnique(column.isUnique());
			}
			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.Sequence.autoincrement)) {
				columnPart.setAutoincrement(column.isAutoincrement());
			}
			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.Sequence.sequence_)) {
				// init part
				this.sequenceSettings = new EObjectFlatComboSettings(column,
						DatabasePackage.eINSTANCE.getColumn_Sequence());
				columnPart.initSequence(this.sequenceSettings);
				// set the button mode
				columnPart.setSequenceButtonMode(ButtonsModeEnum.BROWSE);
			}
			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.defaultValue)) {
				columnPart
						.setDefaultValue(EEFConverterUtil.convertToString(
								EcorePackage.Literals.ESTRING,
								column.getDefaultValue()));
			}

			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.comments)) {
				columnPart.setComments(EcoreUtil.convertToString(
						EcorePackage.Literals.ESTRING, column.getComments()));
			}
			if ((this.typeSettings.getSignificantObject() != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.type)) {
				columnPart.initType(EEFUtils.choiceOfValues(this.typeSettings
						.getSignificantObject(), TypesLibraryPackage.eINSTANCE
						.getTypeInstance_NativeType()), this.typeSettings
						.getValue());
			}
			if ((this.lengthSettings.getValue() != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.TypeAttributes.length)) {
				columnPart.setLength(EEFConverterUtil.convertToString(
						EcorePackage.Literals.EINTEGER_OBJECT,
						this.lengthSettings.getValue()));
			}

			if ((this.precisionSettings.getValue() != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.TypeAttributes.precision)) {
				columnPart.setPrecision(EEFConverterUtil.convertToString(
						EcorePackage.Literals.EINTEGER_OBJECT,
						this.precisionSettings.getValue()));
			}

			if ((this.literalsSettings.getSignificantObject() != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.literals)) {
				columnPart.setLiterals((EList<?>) this.literalsSettings
						.getValue());
			}
			// init filters

			if (this.isAccessible(DatabaseViewsRepository.Column.Properties.Sequence.sequence_)) {
				columnPart.addFilterToSequence(new ViewerFilter() {

					/**
					 * {@inheritDoc}
					 * 
					 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
					 *      java.lang.Object, java.lang.Object)
					 */
					@Override
					public boolean select(final Viewer viewer,
							final Object parentElement, final Object element) {
						return ((element instanceof String) && element
								.equals("")) || (element instanceof Sequence); //$NON-NLS-1$ 
					}

				});
				// Start of user code for additional businessfilters for
				// sequence
				// End of user code
			}

			// Start of user code for additional businessfilters for type
			// End of user code

			// init values for referenced views

			// init filters for referenced views

		}
		this.setInitializing(false);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.impl.components.SinglePartPropertiesEditingComponent#shouldProcess(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
	 */
	@Override
	protected boolean shouldProcess(final IPropertiesEditionEvent event) {
		if (event.getAffectedEditor() == DatabaseViewsRepository.Column.Properties.type) {
			return (this.typeSettings.getValue() == null) ? (event
					.getNewValue() != null) : (!this.typeSettings.getValue()
					.equals(event.getNewValue()));
		}
		if (event.getAffectedEditor() == DatabaseViewsRepository.Column.Properties.TypeAttributes.length) {
			return (this.lengthSettings.getValue() == null) ? (event
					.getNewValue() != null) : (!this.lengthSettings.getValue()
					.equals(event.getNewValue()));
		}
		if (event.getAffectedEditor() == DatabaseViewsRepository.Column.Properties.TypeAttributes.precision) {
			return (this.precisionSettings.getValue() == null) ? (event
					.getNewValue() != null) : (!this.precisionSettings
					.getValue().equals(event.getNewValue()));
		}
		if (event.getAffectedEditor() == DatabaseViewsRepository.Column.Properties.literals) {
			return (this.literalsSettings.getValue() == null) ? (event
					.getNewValue() != null) : (!this.literalsSettings
					.getValue().equals(event.getNewValue()));
		}
		return super.shouldProcess(event);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#associatedFeature(java.lang.Object)
	 */
	@Override
	public EStructuralFeature associatedFeature(final Object editorKey) {
		if (editorKey == DatabaseViewsRepository.Column.Properties.name) {
			return DatabasePackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.nullable) {
			return DatabasePackage.eINSTANCE.getColumn_Nullable();
		}
		if (editorKey == DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.primaryKey) {
			return DatabasePackage.eINSTANCE.getColumn_InPrimaryKey();
		}
		if (editorKey == DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.unique) {
			return DatabasePackage.eINSTANCE.getColumn_Unique();
		}
		if (editorKey == DatabaseViewsRepository.Column.Properties.Sequence.autoincrement) {
			return DatabasePackage.eINSTANCE.getColumn_Autoincrement();
		}
		if (editorKey == DatabaseViewsRepository.Column.Properties.Sequence.sequence_) {
			return DatabasePackage.eINSTANCE.getColumn_Sequence();
		}
		if (editorKey == DatabaseViewsRepository.Column.Properties.defaultValue) {
			return DatabasePackage.eINSTANCE.getColumn_DefaultValue();
		}
		if (editorKey == DatabaseViewsRepository.Column.Properties.comments) {
			return DatabasePackage.eINSTANCE.getDatabaseElement_Comments();
		}
		return super.associatedFeature(editorKey);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#updateSemanticModel(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
	 * 
	 */
	@Override
	public void updateSemanticModel(final IPropertiesEditionEvent event) {
		final Column column = (Column) this.semanticObject;
		if (DatabaseViewsRepository.Column.Properties.name == event
				.getAffectedEditor()) {
			column.setName((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.nullable == event
				.getAffectedEditor()) {
			column.setNullable((Boolean) event.getNewValue());
		}
		if (DatabaseViewsRepository.Column.Properties.Sequence.autoincrement == event
				.getAffectedEditor()) {
			column.setAutoincrement((Boolean) event.getNewValue());
		}
		if (DatabaseViewsRepository.Column.Properties.Sequence.sequence_ == event
				.getAffectedEditor()) {
			if (event.getKind() == PropertiesEditionEvent.SET) {
				this.sequenceSettings.setToReference(event.getNewValue());
			} else if (event.getKind() == PropertiesEditionEvent.ADD) {
				final Sequence eObject = DatabaseFactory.eINSTANCE
						.createSequence();
				final EObjectPropertiesEditionContext context = new EObjectPropertiesEditionContext(
						this.editingContext, this, eObject,
						this.editingContext.getAdapterFactory());
				final PropertiesEditingProvider provider = (PropertiesEditingProvider) this.editingContext
						.getAdapterFactory().adapt(eObject,
								PropertiesEditingProvider.class);
				if (provider != null) {
					final PropertiesEditingPolicy policy = provider
							.getPolicy(context);
					if (policy != null) {
						policy.execute();
					}
				}
				this.sequenceSettings.setToReference(eObject);
			}
		}
		if (DatabaseViewsRepository.Column.Properties.defaultValue == event
				.getAffectedEditor()) {
			column.setDefaultValue((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.Column.Properties.comments == event
				.getAffectedEditor()) {
			column.setComments((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.Column.Properties.type == event
				.getAffectedEditor()) {
			this.typeSettings
					.setValue(!"".equals(event.getNewValue()) ? (NativeType) event
							.getNewValue() : null);
		}
		if (DatabaseViewsRepository.Column.Properties.TypeAttributes.length == event
				.getAffectedEditor()) {
			this.lengthSettings.setValue(EEFConverterUtil.createFromString(
					EcorePackage.Literals.EINTEGER_OBJECT,
					(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.Column.Properties.TypeAttributes.precision == event
				.getAffectedEditor()) {
			this.precisionSettings.setValue(EEFConverterUtil.createFromString(
					EcorePackage.Literals.EINTEGER_OBJECT,
					(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.Column.Properties.literals == event
				.getAffectedEditor()) {
			this.literalsSettings.setValue(event.getNewValue());
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#updatePart(org.eclipse.emf.common.notify.Notification)
	 */
	@Override
	public void updatePart(final Notification msg) {
		super.updatePart(msg);
		if (this.editingPart.isVisible()) {
			final ColumnPropertiesEditionPart columnPart = (ColumnPropertiesEditionPart) this.editingPart;
			if (DatabasePackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.name)) {
				if (msg.getNewValue() != null) {
					columnPart.setName(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					columnPart.setName("");
				}
			}
			if (DatabasePackage.eINSTANCE.getColumn_Nullable().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.nullable)) {
				columnPart.setNullable((Boolean) msg.getNewValue());
			}

			if (DatabasePackage.eINSTANCE.getColumn_InPrimaryKey().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.primaryKey)) {
				columnPart.setPrimaryKey((Boolean) msg.getNewValue());
			}

			if (DatabasePackage.eINSTANCE.getColumn_Unique().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.unique)) {
				columnPart.setUnique((Boolean) msg.getNewValue());
			}

			if (DatabasePackage.eINSTANCE.getColumn_Autoincrement().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.Sequence.autoincrement)) {
				columnPart.setAutoincrement((Boolean) msg.getNewValue());
			}

			if (DatabasePackage.eINSTANCE.getColumn_Sequence().equals(
					msg.getFeature())
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.Sequence.sequence_)) {
				columnPart.setSequence((EObject) msg.getNewValue());
			}
			if (DatabasePackage.eINSTANCE.getColumn_DefaultValue().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.defaultValue)) {
				if (msg.getNewValue() != null) {
					columnPart.setDefaultValue(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					columnPart.setDefaultValue("");
				}
			}
			if (DatabasePackage.eINSTANCE.getDatabaseElement_Comments().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.comments)) {
				if (msg.getNewValue() != null) {
					columnPart.setComments(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					columnPart.setComments("");
				}
			}
			if (this.typeSettings.isAffectingEvent(msg)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.type)) {
				columnPart.setType(msg.getNewValue());
			}
			if (!(msg.getNewValue() instanceof EObject)
					&& this.lengthSettings.isAffectingEvent(msg)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.TypeAttributes.length)) {
				if (msg.getNewValue() != null) {
					columnPart.setLength(EcoreUtil.convertToString(
							EcorePackage.Literals.EINTEGER_OBJECT,
							msg.getNewValue()));
				} else {
					columnPart.setLength("");
				}
			}
			if (!(msg.getNewValue() instanceof EObject)
					&& this.precisionSettings.isAffectingEvent(msg)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.TypeAttributes.precision)) {
				if (msg.getNewValue() != null) {
					columnPart.setPrecision(EcoreUtil.convertToString(
							EcorePackage.Literals.EINTEGER_OBJECT,
							msg.getNewValue()));
				} else {
					columnPart.setPrecision("");
				}
			}
			if (this.literalsSettings.isAffectingEvent(msg)
					&& (columnPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Column.Properties.literals)) {
				if (msg.getNewValue() instanceof EList<?>) {
					columnPart.setLiterals((EList<?>) msg.getNewValue());
				} else if (msg.getNewValue() == null) {
					columnPart.setLiterals(new BasicEList<Object>());
				} else {
					final BasicEList<Object> newValueAsList = new BasicEList<Object>();
					newValueAsList.add(msg.getNewValue());
					columnPart.setLiterals(newValueAsList);
				}
			}

		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#getNotificationFilters()
	 */
	@Override
	protected NotificationFilter[] getNotificationFilters() {
		final NotificationFilter filter = new EStructuralFeatureNotificationFilter(
				DatabasePackage.eINSTANCE.getNamedElement_Name(),
				DatabasePackage.eINSTANCE.getColumn_Nullable(),
				DatabasePackage.eINSTANCE.getColumn_InPrimaryKey(),
				DatabasePackage.eINSTANCE.getColumn_Unique(),
				DatabasePackage.eINSTANCE.getColumn_Autoincrement(),
				DatabasePackage.eINSTANCE.getColumn_Sequence(),
				DatabasePackage.eINSTANCE.getColumn_DefaultValue(),
				DatabasePackage.eINSTANCE.getDatabaseElement_Comments(),
				TypesLibraryPackage.eINSTANCE.getTypeInstance_NativeType(),
				TypesLibraryPackage.eINSTANCE.getTypeInstance_Length(),
				TypesLibraryPackage.eINSTANCE.getTypeInstance_Precision(),
				TypesLibraryPackage.eINSTANCE.getTypeInstance_Literals());
		return new NotificationFilter[] { filter, };
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#isRequired(java.lang.Object,
	 *      int)
	 * 
	 */
	@Override
	public boolean isRequired(final Object key, final int kind) {
		return (key == DatabaseViewsRepository.Column.Properties.name)
				|| (key == DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.nullable)
				|| (key == DatabaseViewsRepository.Column.Properties.type);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.emf.eef.runtime.api.component.IPropertiesEditionComponent#validateValue(org.eclipse.emf.eef.runtime.api.notify.IPropertiesEditionEvent)
	 * 
	 */
	@Override
	public Diagnostic validateValue(final IPropertiesEditionEvent event) {
		Diagnostic ret = Diagnostic.OK_INSTANCE;
		if (event.getNewValue() != null) {
			try {
				if (DatabaseViewsRepository.Column.Properties.name == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getNamedElement_Name()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getNamedElement_Name()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.nullable == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getColumn_Nullable()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getColumn_Nullable()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.primaryKey == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getColumn_InPrimaryKey()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getColumn_InPrimaryKey()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.NullablePkAndUnique.unique == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(
										DatabasePackage.eINSTANCE
												.getColumn_Unique()
												.getEAttributeType(),
										(String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getColumn_Unique()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.Sequence.autoincrement == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getColumn_Autoincrement()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getColumn_Autoincrement()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.defaultValue == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getColumn_DefaultValue()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getColumn_DefaultValue()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.comments == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getDatabaseElement_Comments()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE
									.getDatabaseElement_Comments()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.TypeAttributes.length == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(TypesLibraryPackage.eINSTANCE
										.getTypeInstance_Length()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							TypesLibraryPackage.eINSTANCE
									.getTypeInstance_Length()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.TypeAttributes.precision == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(TypesLibraryPackage.eINSTANCE
										.getTypeInstance_Precision()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							TypesLibraryPackage.eINSTANCE
									.getTypeInstance_Precision()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Column.Properties.literals == event
						.getAffectedEditor()) {
					final BasicDiagnostic chain = new BasicDiagnostic();
					for (final Iterator iterator = ((List) event.getNewValue())
							.iterator(); iterator.hasNext();) {
						chain.add(Diagnostician.INSTANCE.validate(
								TypesLibraryPackage.eINSTANCE
										.getTypeInstance_Literals()
										.getEAttributeType(), iterator.next()));
					}
					ret = chain;
				}
			} catch (final IllegalArgumentException iae) {
				ret = BasicDiagnostic.toDiagnostic(iae);
			} catch (final WrappedException we) {
				ret = BasicDiagnostic.toDiagnostic(we);
			}
		}
		return ret;
	}

	/**
	 * @ return settings for type editor
	 */
	public EEFEditorSettingsImpl getTypeSettings() {
		return this.typeSettings;
	}

	/**
	 * @ return settings for length editor
	 */
	public EEFEditorSettingsImpl getLengthSettings() {
		return this.lengthSettings;
	}

	/**
	 * @ return settings for precision editor
	 */
	public EEFEditorSettingsImpl getPrecisionSettings() {
		return this.precisionSettings;
	}

	/**
	 * @ return settings for literals editor
	 */
	public EEFEditorSettingsImpl getLiteralsSettings() {
		return this.literalsSettings;
	}

}

