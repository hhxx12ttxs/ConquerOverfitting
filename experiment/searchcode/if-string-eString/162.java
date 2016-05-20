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
import org.eclipse.emf.eef.runtime.context.impl.EObjectPropertiesEditionContext;
import org.eclipse.emf.eef.runtime.impl.components.SinglePartPropertiesEditingComponent;
import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
import org.eclipse.emf.eef.runtime.policies.PropertiesEditingPolicy;
import org.eclipse.emf.eef.runtime.providers.PropertiesEditingProvider;
import org.eclipse.emf.eef.runtime.ui.widgets.ButtonsModeEnum;
import org.eclipse.emf.eef.runtime.ui.widgets.eobjflatcombo.EObjectFlatComboSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.obeonetwork.dsl.database.Column;
import org.obeonetwork.dsl.database.DatabaseFactory;
import org.obeonetwork.dsl.database.DatabasePackage;
import org.obeonetwork.dsl.database.IndexElement;
import org.obeonetwork.dsl.database.parts.DatabaseViewsRepository;
import org.obeonetwork.dsl.database.parts.IndexElementPropertiesEditionPart;

// End of user code

/**
 * 
 * 
 */
public class IndexElementPropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String INDEXELEMENT_PART = "Index Element"; //$NON-NLS-1$

	/**
	 * Settings for column EObjectFlatComboViewer
	 */
	private EObjectFlatComboSettings columnSettings;

	/**
	 * Default constructor
	 * 
	 */
	public IndexElementPropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject indexElement, final String editing_mode) {
		super(editingContext, indexElement, editing_mode);
		this.parts = new String[] { INDEXELEMENT_PART };
		this.repositoryKey = DatabaseViewsRepository.class;
		this.partKey = DatabaseViewsRepository.IndexElement.class;
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

			final IndexElement indexElement = (IndexElement) elt;
			final IndexElementPropertiesEditionPart indexElementPart = (IndexElementPropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(DatabaseViewsRepository.IndexElement.Properties.column)) {
				// init part
				this.columnSettings = new EObjectFlatComboSettings(
						indexElement,
						DatabasePackage.eINSTANCE.getIndexElement_Column());
				indexElementPart.initColumn(this.columnSettings);
				// set the button mode
				indexElementPart.setColumnButtonMode(ButtonsModeEnum.BROWSE);
			}
			if (this.isAccessible(DatabaseViewsRepository.IndexElement.Properties.asc)) {
				indexElementPart.setAsc(indexElement.isAsc());
			}
			if (this.isAccessible(DatabaseViewsRepository.IndexElement.Properties.comments)) {
				indexElementPart.setComments(EcoreUtil.convertToString(
						EcorePackage.Literals.ESTRING,
						indexElement.getComments()));
			}
			// init filters
			if (this.isAccessible(DatabaseViewsRepository.IndexElement.Properties.column)) {
				indexElementPart.addFilterToColumn(new ViewerFilter() {

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
								.equals("")) || (element instanceof Column); //$NON-NLS-1$ 
					}

				});
				// Start of user code for additional businessfilters for column
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
		if (editorKey == DatabaseViewsRepository.IndexElement.Properties.column) {
			return DatabasePackage.eINSTANCE.getIndexElement_Column();
		}
		if (editorKey == DatabaseViewsRepository.IndexElement.Properties.asc) {
			return DatabasePackage.eINSTANCE.getIndexElement_Asc();
		}
		if (editorKey == DatabaseViewsRepository.IndexElement.Properties.comments) {
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
		final IndexElement indexElement = (IndexElement) this.semanticObject;
		if (DatabaseViewsRepository.IndexElement.Properties.column == event
				.getAffectedEditor()) {
			if (event.getKind() == PropertiesEditionEvent.SET) {
				this.columnSettings.setToReference(event.getNewValue());
			} else if (event.getKind() == PropertiesEditionEvent.ADD) {
				final Column eObject = DatabaseFactory.eINSTANCE.createColumn();
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
				this.columnSettings.setToReference(eObject);
			}
		}
		if (DatabaseViewsRepository.IndexElement.Properties.asc == event
				.getAffectedEditor()) {
			indexElement.setAsc((Boolean) event.getNewValue());
		}
		if (DatabaseViewsRepository.IndexElement.Properties.comments == event
				.getAffectedEditor()) {
			indexElement.setComments((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
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
			final IndexElementPropertiesEditionPart indexElementPart = (IndexElementPropertiesEditionPart) this.editingPart;
			if (DatabasePackage.eINSTANCE.getIndexElement_Column().equals(
					msg.getFeature())
					&& (indexElementPart != null)
					&& this.isAccessible(DatabaseViewsRepository.IndexElement.Properties.column)) {
				indexElementPart.setColumn((EObject) msg.getNewValue());
			}
			if (DatabasePackage.eINSTANCE.getIndexElement_Asc().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (indexElementPart != null)
					&& this.isAccessible(DatabaseViewsRepository.IndexElement.Properties.asc)) {
				indexElementPart.setAsc((Boolean) msg.getNewValue());
			}

			if (DatabasePackage.eINSTANCE.getDatabaseElement_Comments().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (indexElementPart != null)
					&& this.isAccessible(DatabaseViewsRepository.IndexElement.Properties.comments)) {
				if (msg.getNewValue() != null) {
					indexElementPart.setComments(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					indexElementPart.setComments("");
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
				DatabasePackage.eINSTANCE.getIndexElement_Column(),
				DatabasePackage.eINSTANCE.getIndexElement_Asc(),
				DatabasePackage.eINSTANCE.getDatabaseElement_Comments());
		return new NotificationFilter[] { filter, };
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
				if (DatabaseViewsRepository.IndexElement.Properties.asc == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getIndexElement_Asc()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE.getIndexElement_Asc()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.IndexElement.Properties.comments == event
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

