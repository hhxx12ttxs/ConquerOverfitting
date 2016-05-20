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
import org.obeonetwork.dsl.interaction.CallMessage;
import org.obeonetwork.dsl.interaction.InteractionPackage;
import org.obeonetwork.dsl.interaction.parts.CallMessagePropertiesEditionPart;
import org.obeonetwork.dsl.interaction.parts.InteractionViewsRepository;

// End of user code

/**
 * 
 * 
 */
public class CallMessageCallMessagePropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String CALLMESSAGE_PART = "CallMessage"; //$NON-NLS-1$

	/**
	 * Default constructor
	 * 
	 */
	public CallMessageCallMessagePropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject callMessage, final String editing_mode) {
		super(editingContext, callMessage, editing_mode);
		this.parts = new String[] { CALLMESSAGE_PART };
		this.repositoryKey = InteractionViewsRepository.class;
		this.partKey = InteractionViewsRepository.CallMessage.class;
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

			final CallMessage callMessage = (CallMessage) elt;
			final CallMessagePropertiesEditionPart callMessagePart = (CallMessagePropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(InteractionViewsRepository.CallMessage.Properties.name)) {
				callMessagePart.setName(EEFConverterUtil.convertToString(
						EcorePackage.Literals.ESTRING, callMessage.getName()));
			}

			if (this.isAccessible(InteractionViewsRepository.CallMessage.Properties.description)) {
				callMessagePart.setDescription(EEFConverterUtil
						.convertToString(EcorePackage.Literals.ESTRING,
								callMessage.getDescription()));
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
		if (editorKey == InteractionViewsRepository.CallMessage.Properties.name) {
			return InteractionPackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == InteractionViewsRepository.CallMessage.Properties.description) {
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
		final CallMessage callMessage = (CallMessage) this.semanticObject;
		if (InteractionViewsRepository.CallMessage.Properties.name == event
				.getAffectedEditor()) {
			callMessage.setName((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (InteractionViewsRepository.CallMessage.Properties.description == event
				.getAffectedEditor()) {
			callMessage.setDescription((java.lang.String) EEFConverterUtil
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
			final CallMessagePropertiesEditionPart callMessagePart = (CallMessagePropertiesEditionPart) this.editingPart;
			if (InteractionPackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (callMessagePart != null)
					&& this.isAccessible(InteractionViewsRepository.CallMessage.Properties.name)) {
				if (msg.getNewValue() != null) {
					callMessagePart.setName(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					callMessagePart.setName("");
				}
			}
			if (EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description()
					.equals(msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (callMessagePart != null)
					&& this.isAccessible(InteractionViewsRepository.CallMessage.Properties.description)) {
				if (msg.getNewValue() != null) {
					callMessagePart.setDescription(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					callMessagePart.setDescription("");
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
				if (InteractionViewsRepository.CallMessage.Properties.name == event
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
				if (InteractionViewsRepository.CallMessage.Properties.description == event
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

