/**
 * Generated with Acceleo
 */
package org.obeonetwork.dsl.interaction.components;

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
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.obeonetwork.dsl.interaction.Interaction;
import org.obeonetwork.dsl.interaction.InteractionFactory;
import org.obeonetwork.dsl.interaction.InteractionPackage;
import org.obeonetwork.dsl.interaction.InteractionUse;
import org.obeonetwork.dsl.interaction.parts.InteractionUsePropertiesEditionPart;
import org.obeonetwork.dsl.interaction.parts.InteractionViewsRepository;

// End of user code

/**
 * 
 * 
 */
public class InteractionUseInteractionUsePropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String INTERACTIONUSE_PART = "InteractionUse"; //$NON-NLS-1$

	/**
	 * Settings for interaction EObjectFlatComboViewer
	 */
	private EObjectFlatComboSettings interactionSettings;

	/**
	 * Default constructor
	 * 
	 */
	public InteractionUseInteractionUsePropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject interactionUse, final String editing_mode) {
		super(editingContext, interactionUse, editing_mode);
		this.parts = new String[] { INTERACTIONUSE_PART };
		this.repositoryKey = InteractionViewsRepository.class;
		this.partKey = InteractionViewsRepository.InteractionUse.class;
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

			final InteractionUse interactionUse = (InteractionUse) elt;
			final InteractionUsePropertiesEditionPart interactionUsePart = (InteractionUsePropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.name)) {
				interactionUsePart
						.setName(EEFConverterUtil.convertToString(
								EcorePackage.Literals.ESTRING,
								interactionUse.getName()));
			}

			if (this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.type)) {
				interactionUsePart
						.setType(EEFConverterUtil.convertToString(
								EcorePackage.Literals.ESTRING,
								interactionUse.getType()));
			}

			if (this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.interaction_)) {
				// init part
				this.interactionSettings = new EObjectFlatComboSettings(
						interactionUse,
						InteractionPackage.eINSTANCE
								.getInteractionUse_Interaction());
				interactionUsePart.initInteraction(this.interactionSettings);
				// set the button mode
				interactionUsePart
						.setInteractionButtonMode(ButtonsModeEnum.BROWSE);
			}
			if (this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.description)) {
				interactionUsePart.setDescription(EEFConverterUtil
						.convertToString(EcorePackage.Literals.ESTRING,
								interactionUse.getDescription()));
			}

			// init filters

			if (this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.interaction_)) {
				interactionUsePart.addFilterToInteraction(new ViewerFilter() {

					/**
					 * {@inheritDoc}
					 * 
					 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer,
					 *      java.lang.Object, java.lang.Object)
					 */
					@Override
					public boolean select(final Viewer viewer,
							final Object parentElement, final Object element) {
						return (element instanceof Interaction);
					}

				});
				// Start of user code for additional businessfilters for
				// interaction
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
		if (editorKey == InteractionViewsRepository.InteractionUse.Properties.name) {
			return InteractionPackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == InteractionViewsRepository.InteractionUse.Properties.type) {
			return InteractionPackage.eINSTANCE.getInteractionUse_Type();
		}
		if (editorKey == InteractionViewsRepository.InteractionUse.Properties.interaction_) {
			return InteractionPackage.eINSTANCE.getInteractionUse_Interaction();
		}
		if (editorKey == InteractionViewsRepository.InteractionUse.Properties.description) {
			return EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description();
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
		final InteractionUse interactionUse = (InteractionUse) this.semanticObject;
		if (InteractionViewsRepository.InteractionUse.Properties.name == event
				.getAffectedEditor()) {
			interactionUse.setName((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (InteractionViewsRepository.InteractionUse.Properties.type == event
				.getAffectedEditor()) {
			interactionUse.setType((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (InteractionViewsRepository.InteractionUse.Properties.interaction_ == event
				.getAffectedEditor()) {
			if (event.getKind() == PropertiesEditionEvent.SET) {
				this.interactionSettings.setToReference(event.getNewValue());
			} else if (event.getKind() == PropertiesEditionEvent.ADD) {
				final Interaction eObject = InteractionFactory.eINSTANCE
						.createInteraction();
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
				this.interactionSettings.setToReference(eObject);
			}
		}
		if (InteractionViewsRepository.InteractionUse.Properties.description == event
				.getAffectedEditor()) {
			interactionUse.setDescription((java.lang.String) EEFConverterUtil
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
			final InteractionUsePropertiesEditionPart interactionUsePart = (InteractionUsePropertiesEditionPart) this.editingPart;
			if (InteractionPackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (interactionUsePart != null)
					&& this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.name)) {
				if (msg.getNewValue() != null) {
					interactionUsePart.setName(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					interactionUsePart.setName("");
				}
			}
			if (InteractionPackage.eINSTANCE.getInteractionUse_Type().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (interactionUsePart != null)
					&& this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.type)) {
				if (msg.getNewValue() != null) {
					interactionUsePart.setType(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					interactionUsePart.setType("");
				}
			}
			if (InteractionPackage.eINSTANCE.getInteractionUse_Interaction()
					.equals(msg.getFeature())
					&& (interactionUsePart != null)
					&& this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.interaction_)) {
				interactionUsePart.setInteraction((EObject) msg.getNewValue());
			}
			if (EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description()
					.equals(msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (interactionUsePart != null)
					&& this.isAccessible(InteractionViewsRepository.InteractionUse.Properties.description)) {
				if (msg.getNewValue() != null) {
					interactionUsePart.setDescription(EcoreUtil
							.convertToString(EcorePackage.Literals.ESTRING,
									msg.getNewValue()));
				} else {
					interactionUsePart.setDescription("");
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
				InteractionPackage.eINSTANCE.getNamedElement_Name(),
				InteractionPackage.eINSTANCE.getInteractionUse_Type(),
				InteractionPackage.eINSTANCE.getInteractionUse_Interaction(),
				EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description());
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
		return (key == InteractionViewsRepository.InteractionUse.Properties.type)
				|| (key == InteractionViewsRepository.InteractionUse.Properties.interaction_);
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
				if (InteractionViewsRepository.InteractionUse.Properties.name == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(InteractionPackage.eINSTANCE
										.getNamedElement_Name()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							InteractionPackage.eINSTANCE.getNamedElement_Name()
									.getEAttributeType(), newValue);
				}
				if (InteractionViewsRepository.InteractionUse.Properties.type == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(InteractionPackage.eINSTANCE
										.getInteractionUse_Type()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							InteractionPackage.eINSTANCE
									.getInteractionUse_Type()
									.getEAttributeType(), newValue);
				}
				if (InteractionViewsRepository.InteractionUse.Properties.description == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(EnvironmentPackage.eINSTANCE
										.getObeoDSMObject_Description()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							EnvironmentPackage.eINSTANCE
									.getObeoDSMObject_Description()
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

