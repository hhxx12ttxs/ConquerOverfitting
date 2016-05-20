package OntoUML.diagram.edit.parts;

import org.eclipse.draw2d.Connection;
import org.eclipse.draw2d.RotatableDecoration;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.util.EList;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ConnectionNodeEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.ITreeBranchEditPart;
import org.eclipse.gmf.runtime.diagram.ui.editpolicies.EditPolicyRoles;
import org.eclipse.gmf.runtime.draw2d.ui.figures.PolylineConnectionEx;
import org.eclipse.gmf.runtime.draw2d.ui.figures.WrappingLabel;
import org.eclipse.gmf.runtime.notation.View;

import OntoUML.Collective;
import OntoUML.Element;
import OntoUML.memberOf;
import OntoUML.Property;

/**
 * @generated
 */
public class MemberOfEditPart extends ConnectionNodeEditPart implements
		ITreeBranchEditPart {

	/**
	 * @generated
	 */
	public static final int VISUAL_ID = 4009;

	/**
	 * @generated
	 */
	public MemberOfEditPart(View view) {
		super(view);
	}

	/**
	 * @generated
	 */
	protected void createDefaultEditPolicies() {
		super.createDefaultEditPolicies();
		installEditPolicy(
				EditPolicyRoles.SEMANTIC_ROLE,
				new OntoUML.diagram.edit.policies.MemberOfItemSemanticEditPolicy());
	}

	/**
	 * @generated
	 */
	protected boolean addFixedChild(EditPart childEditPart) {
		if (childEditPart instanceof OntoUML.diagram.edit.parts.WrappingLabel21EditPart) {
			((OntoUML.diagram.edit.parts.WrappingLabel21EditPart) childEditPart)
					.setLabel(getPrimaryShape().getFigureMemberOfLabelFigure());
			return true;
		}
		if (childEditPart instanceof OntoUML.diagram.edit.parts.MemberOfNameEditPart) {
			((OntoUML.diagram.edit.parts.MemberOfNameEditPart) childEditPart)
					.setLabel(getPrimaryShape()
							.getFigureMemberOfNameLabelFigure());
			return true;
		}
		if (childEditPart instanceof OntoUML.diagram.edit.parts.WrappingLabel22EditPart) {
			((OntoUML.diagram.edit.parts.WrappingLabel22EditPart) childEditPart)
					.setLabel(getPrimaryShape()
							.getFigureMemberOfMetaAttributesLabelFigure());
			return true;
		}
		if (childEditPart instanceof OntoUML.diagram.edit.parts.MemberOfSourcePropertyNameLabelEditPart) {
			((OntoUML.diagram.edit.parts.MemberOfSourcePropertyNameLabelEditPart) childEditPart)
					.setLabel(getPrimaryShape()
							.getFigureMemberOfSourcePropertyNameLabelFigure());
			return true;
		}
		if (childEditPart instanceof OntoUML.diagram.edit.parts.MemberOfSourcePropertyCardinaliEditPart) {
			((OntoUML.diagram.edit.parts.MemberOfSourcePropertyCardinaliEditPart) childEditPart)
					.setLabel(getPrimaryShape()
							.getFigureMemberOfSourcePropertyCardinalitiesLabelFigure());
			return true;
		}
		if (childEditPart instanceof OntoUML.diagram.edit.parts.MemberOfTargetPropertyNameLabelEditPart) {
			((OntoUML.diagram.edit.parts.MemberOfTargetPropertyNameLabelEditPart) childEditPart)
					.setLabel(getPrimaryShape()
							.getFigureMemberOfTargetPropertyNameLabelFigure());
			return true;
		}
		if (childEditPart instanceof OntoUML.diagram.edit.parts.MemberOfTargetPropertyCardinaliEditPart) {
			((OntoUML.diagram.edit.parts.MemberOfTargetPropertyCardinaliEditPart) childEditPart)
					.setLabel(getPrimaryShape()
							.getFigureMemberOfTargetPropertyCardinalitiesLabelFigure());
			return true;
		}
		return false;
	}

	/**
	 * @generated
	 */
	protected void addChildVisual(EditPart childEditPart, int index) {
		if (addFixedChild(childEditPart)) {
			return;
		}
		super.addChildVisual(childEditPart, -1);
	}

	/**
	 * Creates figure for this edit part.
	 * 
	 * Body of this method does not depend on settings in generation model
	 * so you may safely remove <i>generated</i> tag and modify it.
	 * 
	 * @generated
	 */

	/**
	 * <!-- begin-user-doc -->
	 * Changed.
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected Connection createConnectionFigure() {
		return new MemberOfFigure(this);
	}

	/**
	 * @generated
	 */
	public MemberOfFigure getPrimaryShape() {
		return (MemberOfFigure) getFigure();
	}

	/**
	 * <!-- begin-user-doc -->
	 * Created to update the exhibition of the meta-attributes isEssential, isInseparable, isImmutablePart e isImmutableWhole.
	 * <!-- end-user-doc -->
	 * @generated NOT
	 */
	protected void handleNotificationEvent(Notification event) {
		getPrimaryShape().updateContents(this);
		super.handleNotificationEvent(event);
	}

	/**
	 * @generated
	 */
	public class MemberOfFigure extends PolylineConnectionEx {

		/**
		 * @generated
		 */
		private WrappingLabel fFigureMemberOfLabelFigure;
		/**
		 * @generated
		 */
		private WrappingLabel fFigureMemberOfNameLabelFigure;
		/**
		 * @generated
		 */
		private WrappingLabel fFigureMemberOfMetaAttributesLabelFigure;

		/**
		 * @generated
		 */
		private WrappingLabel fFigureMemberOfSourcePropertyNameLabelFigure;
		/**
		 * @generated
		 */
		private WrappingLabel fFigureMemberOfSourcePropertyCardinalitiesLabelFigure;
		/**
		 * @generated
		 */
		private WrappingLabel fFigureMemberOfTargetPropertyNameLabelFigure;
		/**
		 * @generated
		 */
		private WrappingLabel fFigureMemberOfTargetPropertyCardinalitiesLabelFigure;

		/**
		 * @generated
		 */
		public MemberOfFigure() {

			createContents();
			setSourceDecoration(createSourceDecoration());
		}

		/**
		 * @generated
		 */
		private void createContents() {

			fFigureMemberOfLabelFigure = new WrappingLabel();
			fFigureMemberOfLabelFigure.setText("ŤmemberOfť");

			this.add(fFigureMemberOfLabelFigure);

			fFigureMemberOfNameLabelFigure = new WrappingLabel();
			fFigureMemberOfNameLabelFigure.setText("Name");

			this.add(fFigureMemberOfNameLabelFigure);

			fFigureMemberOfMetaAttributesLabelFigure = new WrappingLabel();
			fFigureMemberOfMetaAttributesLabelFigure.setText("{}");

			this.add(fFigureMemberOfMetaAttributesLabelFigure);

			fFigureMemberOfSourcePropertyNameLabelFigure = new WrappingLabel();
			fFigureMemberOfSourcePropertyNameLabelFigure.setText("");

			this.add(fFigureMemberOfSourcePropertyNameLabelFigure);

			fFigureMemberOfSourcePropertyCardinalitiesLabelFigure = new WrappingLabel();
			fFigureMemberOfSourcePropertyCardinalitiesLabelFigure.setText("");

			this.add(fFigureMemberOfSourcePropertyCardinalitiesLabelFigure);

			fFigureMemberOfTargetPropertyNameLabelFigure = new WrappingLabel();
			fFigureMemberOfTargetPropertyNameLabelFigure.setText("");

			this.add(fFigureMemberOfTargetPropertyNameLabelFigure);

			fFigureMemberOfTargetPropertyCardinalitiesLabelFigure = new WrappingLabel();
			fFigureMemberOfTargetPropertyCardinalitiesLabelFigure.setText("");

			this.add(fFigureMemberOfTargetPropertyCardinalitiesLabelFigure);

		}

		/**
		 * <!-- begin-user-doc -->
		 * Changed to receive MemberOfEditPart.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		public MemberOfFigure(MemberOfEditPart memberofeditpart) {

			createContents(memberofeditpart);
			setSourceDecoration(createSourceDecoration());
		}

		/**
		 * <!-- begin-user-doc -->
		 * Tests if the whole is extensional.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected boolean testIsExtensional(memberOf m) {
			for (int i = 0; i < m.getSource().size(); ++i)
				if (!((Collective) ((Property) m.getSource().get(i))
						.getEndType()).isIsExtensional())
					return false;
			return true;
		}

		/**
		 * <!-- begin-user-doc -->
		 * Set isExtensional = true in the whole.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected void setIsExtensionalTrue(memberOf m) {
			for (int i = 0; i < m.getSource().size(); ++i)
				((Collective) ((Property) m.getSource().get(i)).getEndType())
						.setIsExtensional(true);
		}

		/**
		 * <!-- begin-user-doc -->
		 * Associate the correct decoration, depending on the attribute isEssential.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected boolean updateFaceIsEssential(
				MemberOfEditPart memberofeditpart) {
			memberOf m = (memberOf) ((View) memberofeditpart.getModel())
					.getElement();
			if (m.isIsEssential() && !testIsExtensional(m))
				setIsExtensionalTrue(m);
			if (m.isIsEssential() && !m.isIsImmutablePart())
				m.setIsImmutablePart(true);
			return m.isIsEssential();
		}

		/**
		 * <!-- begin-user-doc -->
		 * Associate the correct decoration, depending on the attribute isInseparable.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected boolean updateFaceIsInseparable(
				MemberOfEditPart memberofeditpart) {
			memberOf m = (memberOf) ((View) memberofeditpart.getModel())
					.getElement();
			if (m.isIsInseparable() && !m.isIsImmutableWhole())
				m.setIsImmutableWhole(true);
			return m.isIsInseparable();
		}

		/**
		 * <!-- begin-user-doc -->
		 * Tests if all the Properties are readOnly.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected boolean testReadOnly(EList<Element> e) {
			for (int i = 0; i < e.size(); ++i)
				if (!((Property) e.get(i)).isIsReadOnly())
					return false;
			return true;
		}

		/**
		 * <!-- begin-user-doc -->
		 * Set all Properties isReadOnly attribute to true.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected void setReadOnlyTrue(EList<Element> e) {
			for (int i = 0; i < e.size(); ++i) {
				((Property) e.get(i)).setIsReadOnly(true);
			}
		}

		/**
		 * <!-- begin-user-doc -->
		 * Associate the correct decoration, depending on the attributes isEssential and isImmutablePart.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected boolean updateFaceIsImmutablePart(
				MemberOfEditPart memberofeditpart) {
			memberOf m = (memberOf) ((View) memberofeditpart.getModel())
					.getElement();
			if (m.isIsImmutablePart() && !testReadOnly(m.getTarget()))
				setReadOnlyTrue(m.getTarget());
			if (!m.isIsEssential() && m.isIsImmutablePart())
				return true;
			else
				return false;
		}

		/**
		 * <!-- begin-user-doc -->
		 * Associate the correct decoration, depending on the attributes isInseparable e isImmutableWhole.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		protected boolean updateFaceIsImmutableWhole(
				MemberOfEditPart memberofeditpart) {
			memberOf m = (memberOf) ((View) memberofeditpart.getModel())
					.getElement();
			if (m.isIsImmutableWhole() && !testReadOnly(m.getSource()))
				setReadOnlyTrue(m.getSource());
			if (!m.isIsInseparable() && m.isIsImmutableWhole())
				return true;
			else
				return false;
		}

		/**
		 * <!-- begin-user-doc -->
		 * Changed.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		private void createContents(MemberOfEditPart memberofeditpart) {

			String text = new String("{");
			Boolean virgula = new Boolean(false);

			fFigureMemberOfLabelFigure = new WrappingLabel();
			fFigureMemberOfLabelFigure.setText("ŤmemberOfť");

			this.add(fFigureMemberOfLabelFigure);

			fFigureMemberOfNameLabelFigure = new WrappingLabel();
			fFigureMemberOfNameLabelFigure.setText("Name");

			this.add(fFigureMemberOfNameLabelFigure);

			fFigureMemberOfMetaAttributesLabelFigure = new WrappingLabel();
			if (updateFaceIsEssential(memberofeditpart)) {
				text = text.concat("essential");
				virgula = true;
			}
			if (updateFaceIsInseparable(memberofeditpart)) {
				if (virgula)
					text = text.concat(",");
				text = text.concat("inseparable");
				virgula = true;

			}
			if (updateFaceIsImmutablePart(memberofeditpart)) {
				if (virgula)
					text = text.concat(",");
				text = text.concat("immutable part");
				virgula = true;
			}
			if (updateFaceIsImmutableWhole(memberofeditpart)) {
				if (virgula)
					text = text.concat(",");
				text = text.concat("immutable whole");
			}
			if (text.equals("{"))
				text = "";
			else
				text = text.concat("}");
			fFigureMemberOfMetaAttributesLabelFigure.setText(text);
			this.add(fFigureMemberOfMetaAttributesLabelFigure);

			fFigureMemberOfSourcePropertyNameLabelFigure = new WrappingLabel();
			fFigureMemberOfSourcePropertyNameLabelFigure.setText("");

			this.add(fFigureMemberOfSourcePropertyNameLabelFigure);

			fFigureMemberOfSourcePropertyCardinalitiesLabelFigure = new WrappingLabel();
			fFigureMemberOfSourcePropertyCardinalitiesLabelFigure.setText("");

			this.add(fFigureMemberOfSourcePropertyCardinalitiesLabelFigure);

			fFigureMemberOfTargetPropertyNameLabelFigure = new WrappingLabel();
			fFigureMemberOfTargetPropertyNameLabelFigure.setText("");

			this.add(fFigureMemberOfTargetPropertyNameLabelFigure);

			fFigureMemberOfTargetPropertyCardinalitiesLabelFigure = new WrappingLabel();
			fFigureMemberOfTargetPropertyCardinalitiesLabelFigure.setText("");

			this.add(fFigureMemberOfTargetPropertyCardinalitiesLabelFigure);

		}

		/**
		 * <!-- begin-user-doc -->
		 * Created to update the exhibition of the meta-attributes isEssential, isInseparable, isImmutablePart e isImmutableWhole.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		private void updateContents(MemberOfEditPart memberofeditpart) {

			String text = new String("{");
			Boolean virgula = new Boolean(false);

			if (updateFaceIsEssential(memberofeditpart)) {
				text = text.concat("essential");
				virgula = true;
			}
			if (updateFaceIsInseparable(memberofeditpart)) {
				if (virgula)
					text = text.concat(",");
				text = text.concat("inseparable");
				virgula = true;

			}
			if (updateFaceIsImmutablePart(memberofeditpart)) {
				if (virgula)
					text = text.concat(",");
				text = text.concat("immutable part");
				virgula = true;
			}
			if (updateFaceIsImmutableWhole(memberofeditpart)) {
				if (virgula)
					text = text.concat(",");
				text = text.concat("immutable whole");
			}
			if (text.equals("{"))
				text = "";
			else
				text = text.concat("}");
			fFigureMemberOfMetaAttributesLabelFigure.setText(text);
			this.add(fFigureMemberOfMetaAttributesLabelFigure);
		}

		/**
		 * <!-- begin-user-doc -->
		 * Changed so that the meta-attribute isShareable set if the diamond will be empty or full.
		 * <!-- end-user-doc -->
		 * @generated NOT
		 */
		private RotatableDecoration createSourceDecoration() {
			OntoUML.diagram.edit.parts.MemberOfCustomFigure df = new OntoUML.diagram.edit.parts.MemberOfCustomFigure(
					MemberOfEditPart.this);

			return df;
		}

		/**
		 * @generated
		 */
		public WrappingLabel getFigureMemberOfLabelFigure() {
			return fFigureMemberOfLabelFigure;
		}

		/**
		 * @generated
		 */
		public WrappingLabel getFigureMemberOfNameLabelFigure() {
			return fFigureMemberOfNameLabelFigure;
		}

		/**
		 * @generated
		 */
		public WrappingLabel getFigureMemberOfMetaAttributesLabelFigure() {
			return fFigureMemberOfMetaAttributesLabelFigure;
		}

		/**
		 * @generated
		 */
		public WrappingLabel getFigureMemberOfSourcePropertyNameLabelFigure() {
			return fFigureMemberOfSourcePropertyNameLabelFigure;
		}

		/**
		 * @generated
		 */
		public WrappingLabel getFigureMemberOfSourcePropertyCardinalitiesLabelFigure() {
			return fFigureMemberOfSourcePropertyCardinalitiesLabelFigure;
		}

		/**
		 * @generated
		 */
		public WrappingLabel getFigureMemberOfTargetPropertyNameLabelFigure() {
			return fFigureMemberOfTargetPropertyNameLabelFigure;
		}

		/**
		 * @generated
		 */
		public WrappingLabel getFigureMemberOfTargetPropertyCardinalitiesLabelFigure() {
			return fFigureMemberOfTargetPropertyCardinalitiesLabelFigure;
		}

	}

}

