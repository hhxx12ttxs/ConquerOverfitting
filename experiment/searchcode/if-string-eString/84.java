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
import org.eclipse.emf.eef.runtime.impl.utils.EEFConverterUtil;
import org.obeonetwork.dsl.database.Constraint;
import org.obeonetwork.dsl.database.DatabasePackage;
import org.obeonetwork.dsl.database.parts.ConstraintPropertiesEditionPart;
import org.obeonetwork.dsl.database.parts.DatabaseViewsRepository;

// End of user code

/**
 * 
 * 
 */
public class ConstraintPropertiesEditionComponent extends
		SinglePartPropertiesEditingComponent {

	public static String CONSTRAINT_PART = "Constraint"; //$NON-NLS-1$

	/**
	 * Default constructor
	 * 
	 */
	public ConstraintPropertiesEditionComponent(
			final PropertiesEditingContext editingContext,
			final EObject constraint, final String editing_mode) {
		super(editingContext, constraint, editing_mode);
		this.parts = new String[] { CONSTRAINT_PART };
		this.repositoryKey = DatabaseViewsRepository.class;
		this.partKey = DatabaseViewsRepository.Constraint.class;
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

			final Constraint constraint = (Constraint) elt;
			final ConstraintPropertiesEditionPart constraintPart = (ConstraintPropertiesEditionPart) this.editingPart;
			// init values
			if (this.isAccessible(DatabaseViewsRepository.Constraint.Properties.name)) {
				constraintPart.setName(EEFConverterUtil.convertToString(
						EcorePackage.Literals.ESTRING, constraint.getName()));
			}

			if (this.isAccessible(DatabaseViewsRepository.Constraint.Properties.expression)) {
				constraintPart.setExpression(EcoreUtil.convertToString(
						EcorePackage.Literals.ESTRING,
						constraint.getExpression()));
			}
			if (this.isAccessible(DatabaseViewsRepository.Constraint.Properties.comments)) {
				constraintPart
						.setComments(EcoreUtil.convertToString(
								EcorePackage.Literals.ESTRING,
								constraint.getComments()));
				// init filters
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
		if (editorKey == DatabaseViewsRepository.Constraint.Properties.name) {
			return DatabasePackage.eINSTANCE.getNamedElement_Name();
		}
		if (editorKey == DatabaseViewsRepository.Constraint.Properties.expression) {
			return DatabasePackage.eINSTANCE.getConstraint_Expression();
		}
		if (editorKey == DatabaseViewsRepository.Constraint.Properties.comments) {
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
		final Constraint constraint = (Constraint) this.semanticObject;
		if (DatabaseViewsRepository.Constraint.Properties.name == event
				.getAffectedEditor()) {
			constraint.setName((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.Constraint.Properties.expression == event
				.getAffectedEditor()) {
			constraint.setExpression((java.lang.String) EEFConverterUtil
					.createFromString(EcorePackage.Literals.ESTRING,
							(String) event.getNewValue()));
		}
		if (DatabaseViewsRepository.Constraint.Properties.comments == event
				.getAffectedEditor()) {
			constraint.setComments((java.lang.String) EEFConverterUtil
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
			final ConstraintPropertiesEditionPart constraintPart = (ConstraintPropertiesEditionPart) this.editingPart;
			if (DatabasePackage.eINSTANCE.getNamedElement_Name().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (constraintPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Constraint.Properties.name)) {
				if (msg.getNewValue() != null) {
					constraintPart.setName(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					constraintPart.setName("");
				}
			}
			if (DatabasePackage.eINSTANCE.getConstraint_Expression().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (constraintPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Constraint.Properties.expression)) {
				if (msg.getNewValue() != null) {
					constraintPart.setExpression(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					constraintPart.setExpression("");
				}
			}
			if (DatabasePackage.eINSTANCE.getDatabaseElement_Comments().equals(
					msg.getFeature())
					&& msg.getNotifier().equals(this.semanticObject)
					&& (constraintPart != null)
					&& this.isAccessible(DatabaseViewsRepository.Constraint.Properties.comments)) {
				if (msg.getNewValue() != null) {
					constraintPart.setComments(EcoreUtil.convertToString(
							EcorePackage.Literals.ESTRING, msg.getNewValue()));
				} else {
					constraintPart.setComments("");
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
				DatabasePackage.eINSTANCE.getConstraint_Expression(),
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
		return key == DatabaseViewsRepository.Constraint.Properties.name;
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
				if (DatabaseViewsRepository.Constraint.Properties.name == event
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
				if (DatabaseViewsRepository.Constraint.Properties.expression == event
						.getAffectedEditor()) {
					Object newValue = event.getNewValue();
					if (newValue instanceof String) {
						newValue = EEFConverterUtil
								.createFromString(DatabasePackage.eINSTANCE
										.getConstraint_Expression()
										.getEAttributeType(), (String) newValue);
					}
					ret = Diagnostician.INSTANCE.validate(
							DatabasePackage.eINSTANCE
									.getConstraint_Expression()
									.getEAttributeType(), newValue);
				}
				if (DatabaseViewsRepository.Constraint.Properties.comments == event
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

