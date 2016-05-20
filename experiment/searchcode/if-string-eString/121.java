/**
 * Generated with Acceleo
 */
package org.obeonetwork.dsl.database.components;

// Start of user code for imports
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
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
import org.eclipse.emf.eef.runtime.impl.components.SinglePartPropertiesEditingComponent;
import org.eclipse.emf.eef.runtime.impl.filters.EObjectStrictFilter;
import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
import org.eclipse.emf.eef.runtime.impl.parts.CompositePropertiesEditionPart;
import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettingsBuilder;
import org.eclipse.emf.eef.runtime.ui.widgets.settings.EEFEditorSettingsBuilder.EEFEditorSettingsImpl;
import org.eclipse.emf.eef.runtime.ui.widgets.settings.NavigationStepBuilder;
import org.obeonetwork.dsl.database.Column;
import org.obeonetwork.dsl.database.DatabasePackage;
import org.obeonetwork.dsl.database.Table;
import org.obeonetwork.dsl.database.parts.DatabaseViewsRepository;
import org.obeonetwork.dsl.database.parts.PrimaryKeyPropertiesEditionPart;

// End of user code

/**
 * 
 * 
 */
public class TablePrimaryKeyPropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String PRIMARYKEY_PART = "Primary Key"; //$NON-NLS-1$

	/**
	 * Settings for pkColumns ReferencesTable
	 */
	private ReferencesTableSettings pkColumnsSettings;

	/**
	 * Settings for pkName editor
	 */
	protected EEFEditorSettingsImpl pkNameSettings = (EEFEditorSettingsImpl) EEFEditorSettingsBuilder
			.create(this.semanticObject,
					DatabasePackage.eINSTANCE.getNamedElement_Name())
			.nextStep(
					NavigationStepBuilder
							.create(DatabasePackage.eINSTANCE
									.getTable_PrimaryKey()).index(0).build())
			.build();

	/**
	 * Settings for pkComments editor
	 */
	protected EEFEditorSettingsImpl pkCommentsSettings = (EEFEditorSettingsImpl) EEFEditorSettingsBuilder
			.create(this.semanticObject,
					DatabasePackage.eINSTANCE.getDatabaseElement_Comments())
			.nextStep(
					NavigationStepBuilder
							.create(DatabasePackage.eINSTANCE
									.getTable_PrimaryKey()).index(0).build())
			.build();

	/**
	 * Default constructor
	 * 
	 */
	public TablePrimaryKeyPropertiesEditionComponent(
			final PropertiesEditingContext editingContext, final EObject table,
			final String editing_mode) {
		super(editingContext, table, editing_mode);
		this.parts = new String[] { PRIMARYKEY_PART };
		this.repositoryKey = DatabaseViewsRepository.class;
		this.partKey = DatabaseViewsRepository.PrimaryKey.class;
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
						.getSettings().add(this.pkNameSettings);
				((CompositePropertiesEditionPart) this.editingPart)
						.getSettings().add(this.pkColumnsSettings);
				((CompositePropertiesEditionPart) this.editingPart)
						.getSettings().add(this.pkCommentsSettings);
			}
			final Table table = (Table) elt;
			final PrimaryKeyPropertiesEditionPart primaryKeyPart = (PrimaryKeyPropertiesEditionPart) this.editingPart;
			// init values
			if ((this.pkNameSettings.getValue() != null)
					&& this.isAccessible(DatabaseViewsRepository.PrimaryKey.Properties.name)) {
				primaryKeyPart.setName(EEFConverterUtil.convertToString(
						EcorePackage.Literals.ESTRING,
						this.pkNameSettings.getValue()));
			}

			if (this.isAccessible(DatabaseViewsRepository.PrimaryKey.Properties.columns)) {
				this.pkColumnsSettings = new ReferencesTableSettings(table,
						DatabasePackage.eINSTANCE.getTable_PrimaryKey(),
						DatabasePackage.eINSTANCE.getPrimaryKey_Columns());
				primaryKeyPart.initColumns(this.pkColumnsSettings);
			}
			if ((this.pkCommentsSettings.getValue() != null)
					&& this.isAccessible(DatabaseViewsRepository.PrimaryKey.Properties.comments)) {
				primaryKeyPart.setComments(EcoreUtil.convertToString(
						EcorePackage.Literals.ESTRING,
						this.pkCommentsSettings.getValue()));
				// init filters
			}

			if (this.isAccessible(DatabaseViewsRepository.PrimaryKey.Properties.columns)) {
				primaryKeyPart.addFilterToColumns(new EObjectStrictFilter(
						DatabasePackage.Literals.COLUMN));
				// Start of user code for additional businessfilters for
				// pkColumns
				// End of user code
			}

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
		if (event.getAffectedEditor() == DatabaseViewsRepository.PrimaryKey.Properties.name) {
			return (this.pkNameSettings.getValue() == null) ? (event
					.getNewValue() != null) : (!this.pkNameSettings.getValue()
					.equals(event.getNewValue()));
		}
		if (event.getAffectedEditor() == DatabaseViewsRepository.PrimaryKey.Properties.columns) {
			return (this.pkColumnsSettings.getValue() == null) ? (event
					.getNewValue() != null) : (!this.pkColumnsSettings
					.getValue().equals(event.getNewValue()));
		}
		if (event.getAffectedEditor() == DatabaseViewsRepository.PrimaryKey.Properties.comments) {
			return (this.pkCommentsSettings.getValue() == null) ? (event
					.getNewValue() != null) : (!this.pkCommentsSettings
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
		final Table table = (Table) this.semanticObject;
		if (DatabaseViewsRepository.PrimaryKey.Properties.name == event
				.getAffectedEditor()) {
			this.pkNameSettings
					.setValue(EEFConverterUtil.createFromString(
							EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.PrimaryKey.Properties.columns == event
				.getAffectedEditor()) {
			if (event.getKind() == PropertiesEditionEvent.ADD) {
				if (event.getNewValue() instanceof Column) {
					this.pkColumnsSettings.addToReference((EObject) event
							.getNewValue());
				}
			} else if (event.getKind() == PropertiesEditionEvent.REMOVE) {
				this.pkColumnsSettings.removeFromReference((EObject) event
						.getNewValue());
			} else if (event.getKind() == PropertiesEditionEvent.MOVE) {
				this.pkColumnsSettings.move(event.getNewIndex(),
						(Column) event.getNewValue());
			}
		}
		if (DatabaseViewsRepository.PrimaryKey.Properties.comments == event
				.getAffectedEditor()) {
			this.pkCommentsSettings
					.setValue(EEFConverterUtil.createFromString(
							EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
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
			final PrimaryKeyPropertiesEditionPart primaryKeyPart = (PrimaryKeyPropertiesEditionPart) this.editingPart;
			if (!(msg.getNewValue() instanceof EObject)
					&& this.pkNameSettings.isAffectingEvent(msg)
					&& (primaryKeyPart != null)
					&& this.isAccessible(DatabaseViewsRepository.PrimaryKey.Properties.name)) {
				if (msg.getNewValue() != null) {
					primaryKeyPart.setName(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					primaryKeyPart.setName("");
				}
			}
			if (this.pkColumnsSettings
					.isAffectingFeature((EStructuralFeature) msg.getFeature())
					&& this.isAccessible(DatabaseViewsRepository.PrimaryKey.Properties.columns)) {
				primaryKeyPart.updateColumns();
			}
			if (!(msg.getNewValue() instanceof EObject)
					&& this.pkCommentsSettings.isAffectingEvent(msg)
					&& (primaryKeyPart != null)
					&& this.isAccessible(DatabaseViewsRepository.PrimaryKey.Properties.comments)) {
				if (msg.getNewValue() != null) {
					primaryKeyPart.setComments(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					primaryKeyPart.setComments("");
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
				DatabasePackage.eINSTANCE.getPrimaryKey_Columns(),
				DatabasePackage.eINSTANCE.getDatabaseElement_Comments());
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
		return (key == DatabaseViewsRepository.Table.Properties.name)
				|| (key == DatabaseViewsRepository.PrimaryKey.Properties.name);
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
				if (DatabaseViewsRepository.PrimaryKey.Properties.name == event
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
				if (DatabaseViewsRepository.PrimaryKey.Properties.comments == event
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
			} catch (final IllegalArgumentException iae) {
				ret = BasicDiagnostic.toDiagnostic(iae);
			} catch (final WrappedException we) {
				ret = BasicDiagnostic.toDiagnostic(we);
			}
		}
		return ret;
	}

	/**
	 * @ return settings for pkName editor
	 */
	public EEFEditorSettingsImpl getPkNameSettings() {
		return this.pkNameSettings;
	}

	/**
	 * @ return settings for pkComments editor
	 */
	public EEFEditorSettingsImpl getPkCommentsSettings() {
		return this.pkCommentsSettings;
	}

}

