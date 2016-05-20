/**
 * Generated with Acceleo
 */
package org.obeonetwork.dsl.database.components;

// Start of user code for imports
import java.util.List;

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
import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.obeonetwork.dsl.database.DataBase;
import org.obeonetwork.dsl.database.DatabasePackage;
import org.obeonetwork.dsl.database.parts.DataBasePropertiesEditionPart;
import org.obeonetwork.dsl.database.parts.DatabaseViewsRepository;
import org.obeonetwork.dsl.typeslibrary.TypesLibraryPackage;

// End of user code

/**
 * 
 * 
 */
public class DataBasePropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String DATABASE_PART = "DataBase"; //$NON-NLS-1$

	/**
	 * Settings for usedLibraries ReferencesTable
	 */
	private ReferencesTableSettings usedLibrariesSettings;

	/**
	 * Default constructor
	 * 
	 */
	public DataBasePropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject dataBase, final String editing_mode) {
		super(editingContext, dataBase, editing_mode);
		this.parts = new String[] { DATABASE_PART };
		this.repositoryKey = DatabaseViewsRepository.class;
		this.partKey = DatabaseViewsRepository.DataBase_.class;
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

			final DataBase dataBase = (DataBase) elt;
			final DataBasePropertiesEditionPart dataBasePart = (DataBasePropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.name)) {
				dataBasePart.setName(EEFConverterUtil.convertToString(
						EcorePackage.Literals.ESTRING, dataBase.getName()));
			}

			if (this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.url)) {
				dataBasePart.setUrl(EEFConverterUtil.convertToString(
						EcorePackage.Literals.ESTRING, dataBase.getUrl()));
			}

			if (this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.comments)) {
				dataBasePart.setComments(EcoreUtil.convertToString(
						EcorePackage.Literals.ESTRING, dataBase.getComments()));
			}
			if (this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.usedLibraries)) {
				this.usedLibrariesSettings = new ReferencesTableSettings(
						dataBase,
						TypesLibraryPackage.eINSTANCE
								.getTypesLibraryUser_UsedLibraries());
				dataBasePart.initUsedLibraries(this.usedLibrariesSettings);
			}
			// init filters

			if (this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.usedLibraries)) {
				dataBasePart.addFilterToUsedLibraries(new ViewerFilter() {

					/**
					 * {@inheritDoc}
					 * 
					 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
					 *      java.lang.Object, java.lang.Object)
					 */
					@Override
					public boolean select(final Viewer viewer,
							final Object parentElement, final Object element) {
						if (element instanceof EObject) {
							return (!dataBasePart
									.isContainedInUsedLibrariesTable((EObject) element));
						}
						return (element instanceof String)
								&& element.equals("");
					}

				});
				dataBasePart.addFilterToUsedLibraries(new EObjectStrictFilter(
						TypesLibraryPackage.Literals.TYPES_LIBRARY));
				// Start of user code for additional businessfilters for
				// usedLibraries
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
	 * @see org.eclipse.emf.eef.runtime.impl.components.StandardPropertiesEditionComponent#associatedFeature(java.lang.Object)
	 */
	@Override
	public EStructuralFeature associatedFeature(final Object editorKey) {
		if (editorKey == DatabaseViewsRepository.DataBase_.Properties.name) {
			return DatabasePackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == DatabaseViewsRepository.DataBase_.Properties.url) {
			return DatabasePackage.eINSTANCE.getDataBase_Url();
		}
		if (editorKey == DatabaseViewsRepository.DataBase_.Properties.comments) {
			return DatabasePackage.eINSTANCE.getDatabaseElement_Comments();
		}
		if (editorKey == DatabaseViewsRepository.DataBase_.Properties.usedLibraries) {
			return TypesLibraryPackage.eINSTANCE
					.getTypesLibraryUser_UsedLibraries();
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
		final DataBase dataBase = (DataBase) this.semanticObject;
		if (DatabaseViewsRepository.DataBase_.Properties.name == event
				.getAffectedEditor()) {
			dataBase.setName((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.DataBase_.Properties.url == event
				.getAffectedEditor()) {
			dataBase.setUrl((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.DataBase_.Properties.comments == event
				.getAffectedEditor()) {
			dataBase.setComments((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.DataBase_.Properties.usedLibraries == event
				.getAffectedEditor()) {
			if (event.getKind() == PropertiesEditionEvent.SET) {
				this.usedLibrariesSettings.setToReference((List<EObject>) event
						.getNewValue());
			}
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
			final DataBasePropertiesEditionPart dataBasePart = (DataBasePropertiesEditionPart) this.editingPart;
			if (DatabasePackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (dataBasePart != null)
					&& this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.name)) {
				if (msg.getNewValue() != null) {
					dataBasePart.setName(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					dataBasePart.setName("");
				}
			}
			if (DatabasePackage.eINSTANCE.getDataBase_Url().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (dataBasePart != null)
					&& this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.url)) {
				if (msg.getNewValue() != null) {
					dataBasePart.setUrl(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					dataBasePart.setUrl("");
				}
			}
			if (DatabasePackage.eINSTANCE.getDatabaseElement_Comments().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (dataBasePart != null)
					&& this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.comments)) {
				if (msg.getNewValue() != null) {
					dataBasePart.setComments(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					dataBasePart.setComments("");
				}
			}
			if (TypesLibraryPackage.eINSTANCE
					.getTypesLibraryUser_UsedLibraries().equals(
							msg.getFeature())
					&& this.isAccessible(DatabaseViewsRepository.DataBase_.Properties.usedLibraries)) {
				dataBasePart.updateUsedLibraries();
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
				DatabasePackage.eINSTANCE.getDataBase_Url(),
				DatabasePackage.eINSTANCE.getDatabaseElement_Comments(),
				TypesLibraryPackage.eINSTANCE
						.getTypesLibraryUser_UsedLibraries());
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
		return key == DatabaseViewsRepository.DataBase_.Properties.name;
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
				if (DatabaseViewsRepository.DataBase_.Properties.name == event
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
				if (DatabaseViewsRepository.DataBase_.Properties.url == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getDataBase_Url().getEAttributeType(),
										(String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getDataBase_Url()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.DataBase_.Properties.comments == event
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

}

