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
import org.obeonetwork.dsl.interaction.CreateParticipantMessage;
import org.obeonetwork.dsl.interaction.InteractionPackage;
import org.obeonetwork.dsl.interaction.parts.CreateParticipantMessagePropertiesEditionPart;
import org.obeonetwork.dsl.interaction.parts.InteractionViewsRepository;

// End of user code

/**
 * 
 * 
 */
public class CreateParticipantMessageCreateParticipantMessagePropertiesEditionComponent
		extends SinglePartPropertiesEditingComponent {

	public static String CREATEPARTICIPANTMESSAGE_PART = "CreateParticipantMessage"; //$NON-NLS-1$

	/**
	 * Default constructor
	 * 
	 */
	public CreateParticipantMessageCreateParticipantMessagePropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject createParticipantMessage, final String editing_mode) {
		super(editingContext, createParticipantMessage, editing_mode);
		this.parts = new String[] { CREATEPARTICIPANTMESSAGE_PART };
		this.repositoryKey = InteractionViewsRepository.class;
		this.partKey = InteractionViewsRepository.CreateParticipantMessage.class;
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

			final CreateParticipantMessage createParticipantMessage = (CreateParticipantMessage) elt;
			final CreateParticipantMessagePropertiesEditionPart createParticipantMessagePart = (CreateParticipantMessagePropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(InteractionViewsRepository.CreateParticipantMessage.Properties.name)) {
				createParticipantMessagePart.setName(EEFConverterUtil
						.convertToString(EcorePackage.Literals.ESTRING,
								createParticipantMessage.getName()));
			}

			if (this.isAccessible(InteractionViewsRepository.CreateParticipantMessage.Properties.description)) {
				createParticipantMessagePart.setDescription(EEFConverterUtil
						.convertToString(EcorePackage.Literals.ESTRING,
								createParticipantMessage.getDescription()));
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
		if (editorKey == InteractionViewsRepository.CreateParticipantMessage.Properties.name) {
			return InteractionPackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == InteractionViewsRepository.CreateParticipantMessage.Properties.description) {
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
		final CreateParticipantMessage createParticipantMessage = (CreateParticipantMessage) this.semanticObject;
		if (InteractionViewsRepository.CreateParticipantMessage.Properties.name == event
				.getAffectedEditor()) {
			createParticipantMessage
					.setName((java.lang.String) EEFConverterUtil
							.createFromString(EcorePackage.Literals.ESTRING,
									(String) event.getNewValue()));
		}
		if (InteractionViewsRepository.CreateParticipantMessage.Properties.description == event
				.getAffectedEditor()) {
			createParticipantMessage
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
			final CreateParticipantMessagePropertiesEditionPart createParticipantMessagePart = (CreateParticipantMessagePropertiesEditionPart) this.editingPart;
			if (InteractionPackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (createParticipantMessagePart != null)
					&& this.isAccessible(InteractionViewsRepository.CreateParticipantMessage.Properties.name)) {
				if (msg.getNewValue() != null) {
					createParticipantMessagePart.setName(EcoreUtil
							.convertToString(EcorePackage.Literals.ESTRING,
									msg.getNewValue()));
				} else {
					createParticipantMessagePart.setName("");
				}
			}
			if (EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description()
					.equals(msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (createParticipantMessagePart != null)
					&& this.isAccessible(InteractionViewsRepository.CreateParticipantMessage.Properties.description)) {
				if (msg.getNewValue() != null) {
					createParticipantMessagePart.setDescription(EcoreUtil
							.convertToString(EcorePackage.Literals.ESTRING,
									msg.getNewValue()));
				} else {
					createParticipantMessagePart.setDescription("");
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
				if (InteractionViewsRepository.CreateParticipantMessage.Properties.name == event
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
				if (InteractionViewsRepository.CreateParticipantMessage.Properties.description == event
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

