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
import org.eclipse.emf.eef.runtime.impl.components.SinglePartPropertiesEditingComponent;
import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.obeonetwork.dsl.interaction.DestroyParticipantMessage;
import org.obeonetwork.dsl.interaction.InteractionPackage;
import org.obeonetwork.dsl.interaction.parts.DestroyParticipantMessagePropertiesEditionPart;
import org.obeonetwork.dsl.interaction.parts.InteractionViewsRepository;

// End of user code

/**
 * 
 * 
 */
public class DestroyParticipantMessageDestroyParticipantMessagePropertiesEditionComponent
		extends SinglePartPropertiesEditingComponent {

	public static String DESTROYPARTICIPANTMESSAGE_PART = "DestroyParticipantMessage"; //$NON-NLS-1$

	/**
	 * Default constructor
	 * 
	 */
	public DestroyParticipantMessageDestroyParticipantMessagePropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject destroyParticipantMessage, final String editing_mode) {
		super(editingContext, destroyParticipantMessage, editing_mode);
		this.parts = new String[] { DESTROYPARTICIPANTMESSAGE_PART };
		this.repositoryKey = InteractionViewsRepository.class;
		this.partKey = InteractionViewsRepository.DestroyParticipantMessage.class;
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

			final DestroyParticipantMessage destroyParticipantMessage = (DestroyParticipantMessage) elt;
			final DestroyParticipantMessagePropertiesEditionPart destroyParticipantMessagePart = (DestroyParticipantMessagePropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(InteractionViewsRepository.DestroyParticipantMessage.Properties.name)) {
				destroyParticipantMessagePart.setName(EEFConverterUtil
						.convertToString(EcorePackage.Literals.ESTRING,
								destroyParticipantMessage.getName()));
			}

			if (this.isAccessible(InteractionViewsRepository.DestroyParticipantMessage.Properties.description)) {
				destroyParticipantMessagePart.setDescription(EEFConverterUtil
						.convertToString(EcorePackage.Literals.ESTRING,
								destroyParticipantMessage.getDescription()));
			}

			// init filters

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
		if (editorKey == InteractionViewsRepository.DestroyParticipantMessage.Properties.name) {
			return InteractionPackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == InteractionViewsRepository.DestroyParticipantMessage.Properties.description) {
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
		final DestroyParticipantMessage destroyParticipantMessage = (DestroyParticipantMessage) this.semanticObject;
		if (InteractionViewsRepository.DestroyParticipantMessage.Properties.name == event
				.getAffectedEditor()) {
			destroyParticipantMessage
					.setName((java.lang.String) EEFConverterUtil
							.createFromString(EcorePackage.Literals.ESTRING,
									(String) event.getNewValue()));
		}
		if (InteractionViewsRepository.DestroyParticipantMessage.Properties.description == event
				.getAffectedEditor()) {
			destroyParticipantMessage
					.setDescription((java.lang.String) EEFConverterUtil
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
			final DestroyParticipantMessagePropertiesEditionPart destroyParticipantMessagePart = (DestroyParticipantMessagePropertiesEditionPart) this.editingPart;
			if (InteractionPackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (destroyParticipantMessagePart != null)
					&& this.isAccessible(InteractionViewsRepository.DestroyParticipantMessage.Properties.name)) {
				if (msg.getNewValue() != null) {
					destroyParticipantMessagePart.setName(EcoreUtil
							.convertToString(EcorePackage.Literals.ESTRING,
									msg.getNewValue()));
				} else {
					destroyParticipantMessagePart.setName("");
				}
			}
			if (EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description()
					.equals(msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (destroyParticipantMessagePart != null)
					&& this.isAccessible(InteractionViewsRepository.DestroyParticipantMessage.Properties.description)) {
				if (msg.getNewValue() != null) {
					destroyParticipantMessagePart.setDescription(EcoreUtil
							.convertToString(EcorePackage.Literals.ESTRING,
									msg.getNewValue()));
				} else {
					destroyParticipantMessagePart.setDescription("");
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
				EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description());
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
				if (InteractionViewsRepository.DestroyParticipantMessage.Properties.name == event
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
				if (InteractionViewsRepository.DestroyParticipantMessage.Properties.description == event
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

