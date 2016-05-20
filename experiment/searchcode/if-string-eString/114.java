/**
 * Generated with Acceleo
 */
package org.obeonetwork.dsl.statemachine.components;

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
import org.eclipse.emf.eef.runtime.context.impl.EReferencePropertiesEditionContext;
import org.eclipse.emf.eef.runtime.impl.components.SinglePartPropertiesEditingComponent;
import org.eclipse.emf.eef.runtime.impl.notify.PropertiesEditionEvent;
import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
import org.eclipse.emf.eef.runtime.policies.PropertiesEditingPolicy;
import org.eclipse.emf.eef.runtime.policies.impl.CreateEditingPolicy;
import org.eclipse.emf.eef.runtime.providers.PropertiesEditingProvider;
import org.eclipse.emf.eef.runtime.ui.widgets.referencestable.ReferencesTableSettings;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.obeonetwork.dsl.environment.EnvironmentPackage;
import org.obeonetwork.dsl.statemachine.Region;
import org.obeonetwork.dsl.statemachine.StateMachine;
import org.obeonetwork.dsl.statemachine.StateMachinePackage;
import org.obeonetwork.dsl.statemachine.parts.StateMachinePropertiesEditionPart;
import org.obeonetwork.dsl.statemachine.parts.StatemachineViewsRepository;

// End of user code

/**
 * 
 * 
 */
public class StateMachineStateMachinePropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String STATEMACHINE_PART = "StateMachine"; //$NON-NLS-1$

	/**
	 * Settings for regions ReferencesTable
	 */
	protected ReferencesTableSettings regionsSettings;

	/**
	 * Default constructor
	 * 
	 */
	public StateMachineStateMachinePropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject stateMachine, final String editing_mode) {
		super(editingContext, stateMachine, editing_mode);
		this.parts = new String[] { STATEMACHINE_PART };
		this.repositoryKey = StatemachineViewsRepository.class;
		this.partKey = StatemachineViewsRepository.StateMachine_.class;
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

			final StateMachine stateMachine = (StateMachine) elt;
			final StateMachinePropertiesEditionPart stateMachinePart = (StateMachinePropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.description)) {
				stateMachinePart.setDescription(EEFConverterUtil
						.convertToString(EcorePackage.Literals.ESTRING,
								stateMachine.getDescription()));
			}

			if (this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.keywords)) {
				stateMachinePart.setKeywords(stateMachine.getKeywords());
			}

			if (this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.name)) {
				stateMachinePart.setName(EEFConverterUtil.convertToString(
						EcorePackage.Literals.ESTRING, stateMachine.getName()));
			}

			if (this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.regions)) {
				this.regionsSettings = new ReferencesTableSettings(
						stateMachine,
						StateMachinePackage.eINSTANCE.getStateMachine_Regions());
				stateMachinePart.initRegions(this.regionsSettings);
			}
			// init filters

			if (this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.regions)) {
				stateMachinePart.addFilterToRegions(new ViewerFilter() {
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
								.equals("")) || (element instanceof Region); //$NON-NLS-1$ 
					}

				});
				// Start of user code for additional businessfilters for regions
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
		if (editorKey == StatemachineViewsRepository.StateMachine_.Properties.description) {
			return EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description();
		}
		if (editorKey == StatemachineViewsRepository.StateMachine_.Properties.keywords) {
			return EnvironmentPackage.eINSTANCE.getObeoDSMObject_Keywords();
		}
		if (editorKey == StatemachineViewsRepository.StateMachine_.Properties.name) {
			return StateMachinePackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == StatemachineViewsRepository.StateMachine_.Properties.regions) {
			return StateMachinePackage.eINSTANCE.getStateMachine_Regions();
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
		final StateMachine stateMachine = (StateMachine) this.semanticObject;
		if (StatemachineViewsRepository.StateMachine_.Properties.description == event
				.getAffectedEditor()) {
			stateMachine.setDescription((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (StatemachineViewsRepository.StateMachine_.Properties.keywords == event
				.getAffectedEditor()) {
			if (event.getKind() == PropertiesEditionEvent.SET) {
				stateMachine.getKeywords().clear();
				stateMachine.getKeywords()
						.addAll(((EList) event.getNewValue()));
			}
		}
		if (StatemachineViewsRepository.StateMachine_.Properties.name == event
				.getAffectedEditor()) {
			stateMachine.setName((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (StatemachineViewsRepository.StateMachine_.Properties.regions == event
				.getAffectedEditor()) {
			if (event.getKind() == PropertiesEditionEvent.ADD) {
				final EReferencePropertiesEditionContext context = new EReferencePropertiesEditionContext(
						this.editingContext, this, this.regionsSettings,
						this.editingContext.getAdapterFactory());
				final PropertiesEditingProvider provider = (PropertiesEditingProvider) this.editingContext
						.getAdapterFactory().adapt(this.semanticObject,
								PropertiesEditingProvider.class);
				if (provider != null) {
					final PropertiesEditingPolicy policy = provider
							.getPolicy(context);
					if (policy instanceof CreateEditingPolicy) {
						policy.execute();
					}
				}
			} else if (event.getKind() == PropertiesEditionEvent.EDIT) {
				final EObjectPropertiesEditionContext context = new EObjectPropertiesEditionContext(
						this.editingContext, this,
						(EObject) event.getNewValue(),
						this.editingContext.getAdapterFactory());
				final PropertiesEditingProvider provider = (PropertiesEditingProvider) this.editingContext
						.getAdapterFactory().adapt(
								(EObject) event.getNewValue(),
								PropertiesEditingProvider.class);
				if (provider != null) {
					final PropertiesEditingPolicy editionPolicy = provider
							.getPolicy(context);
					if (editionPolicy != null) {
						editionPolicy.execute();
					}
				}
			} else if (event.getKind() == PropertiesEditionEvent.REMOVE) {
				this.regionsSettings.removeFromReference((EObject) event
						.getNewValue());
			} else if (event.getKind() == PropertiesEditionEvent.MOVE) {
				this.regionsSettings.move(event.getNewIndex(),
						(Region) event.getNewValue());
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
			final StateMachinePropertiesEditionPart stateMachinePart = (StateMachinePropertiesEditionPart) this.editingPart;
			if (EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description()
					.equals(msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (stateMachinePart != null)
					&& this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.description)) {
				if (msg.getNewValue() != null) {
					stateMachinePart.setDescription(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					stateMachinePart.setDescription("");
				}
			}
			if (EnvironmentPackage.eINSTANCE.getObeoDSMObject_Keywords()
					.equals(msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (stateMachinePart != null)
					&& this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.keywords)) {
				if (msg.getNewValue() instanceof EList<?>) {
					stateMachinePart.setKeywords((EList<?>) msg.getNewValue());
				} else if (msg.getNewValue() == null) {
					stateMachinePart.setKeywords(new BasicEList<Object>());
				} else {
					final BasicEList<Object> newValueAsList = new BasicEList<Object>();
					newValueAsList.add(msg.getNewValue());
					stateMachinePart.setKeywords(newValueAsList);
				}
			}

			if (StateMachinePackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (stateMachinePart != null)
					&& this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.name)) {
				if (msg.getNewValue() != null) {
					stateMachinePart.setName(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					stateMachinePart.setName("");
				}
			}
			if (StateMachinePackage.eINSTANCE.getStateMachine_Regions().equals(
					msg.getFeature())
					&& this.isAccessible(StatemachineViewsRepository.StateMachine_.Properties.regions)) {
				stateMachinePart.updateRegions();
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
				EnvironmentPackage.eINSTANCE.getObeoDSMObject_Description(),
				EnvironmentPackage.eINSTANCE.getObeoDSMObject_Keywords(),
				StateMachinePackage.eINSTANCE.getNamedElement_Name(),
				StateMachinePackage.eINSTANCE.getStateMachine_Regions());
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
				if (StatemachineViewsRepository.StateMachine_.Properties.description == event
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
				if (StatemachineViewsRepository.StateMachine_.Properties.keywords == event
						.getAffectedEditor()) {
					final BasicDiagnostic chain = new BasicDiagnostic();
					for (final Iterator iterator = ((List) event.getNewValue())
							.iterator(); iterator.hasNext();) {
						chain.add(Diagnostician.INSTANCE.validate(
								EnvironmentPackage.eINSTANCE
										.getObeoDSMObject_Keywords()
										.getEAttributeType(), iterator.next()));
					}
					ret = chain;
				}
				if (StatemachineViewsRepository.StateMachine_.Properties.name == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(StateMachinePackage.eINSTANCE
										.getNamedElement_Name()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE
							.validate(
									StateMachinePackage.eINSTANCE
											.getNamedElement_Name()
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

