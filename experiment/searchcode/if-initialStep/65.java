package plcopen.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import plcopen.inf.model.IActionType;
import plcopen.inf.model.IConfiguration;
import plcopen.inf.model.IContentHeader;
import plcopen.inf.model.ICoordinationInfo;
import plcopen.inf.model.IDataTypeType;
import plcopen.inf.model.IFileHeader;
import plcopen.inf.model.IPLCProject;
import plcopen.inf.model.IPLCTask;
import plcopen.inf.model.IPOU;
import plcopen.inf.model.IResource;
import plcopen.inf.model.ITransitionType;
import plcopen.inf.model.IVariable;
import plcopen.inf.type.DateTime;
import plcopen.inf.type.EdgeModifierType;
import plcopen.inf.type.IArrayValue;
import plcopen.inf.type.IArrayValueType;
import plcopen.inf.type.IBody;
import plcopen.inf.type.IConnection;
import plcopen.inf.type.IConnectionPointIn;
import plcopen.inf.type.IConnectionPointOut;
import plcopen.inf.type.IDataType;
import plcopen.inf.type.IExtendedType;
import plcopen.inf.type.IPOUInstance;
import plcopen.inf.type.IPosition;
import plcopen.inf.type.IRange;
import plcopen.inf.type.ISimpleValue;
import plcopen.inf.type.IStructValue;
import plcopen.inf.type.IStructValueType;
import plcopen.inf.type.IValue;
import plcopen.inf.type.IVariableList;
import plcopen.inf.type.StorageModifierType;
import plcopen.inf.type.body.IFBD;
import plcopen.inf.type.body.IIL;
import plcopen.inf.type.body.ILD;
import plcopen.inf.type.body.ISFC;
import plcopen.inf.type.body.IST;
import plcopen.inf.type.group.common.IAction;
import plcopen.inf.type.group.common.IActionBlock;
import plcopen.inf.type.group.common.IComment;
import plcopen.inf.type.group.common.IConnector;
import plcopen.inf.type.group.common.IContinuation;
import plcopen.inf.type.group.common.IError;
import plcopen.inf.type.group.derived.IArray;
import plcopen.inf.type.group.derived.IDerived;
import plcopen.inf.type.group.derived.IDerivedType;
import plcopen.inf.type.group.derived.IEnum;
import plcopen.inf.type.group.derived.IRangeType;
import plcopen.inf.type.group.derived.IStruct;
import plcopen.inf.type.group.elementary.IElementaryType;
import plcopen.inf.type.group.extended.IPointerType;
import plcopen.inf.type.group.fbd.IBlock;
import plcopen.inf.type.group.fbd.IFBDObject;
import plcopen.inf.type.group.fbd.IInOutVariable;
import plcopen.inf.type.group.fbd.IInOutVariableInBlock;
import plcopen.inf.type.group.fbd.IInVariable;
import plcopen.inf.type.group.fbd.IInVariableInBlock;
import plcopen.inf.type.group.fbd.IJump;
import plcopen.inf.type.group.fbd.ILabel;
import plcopen.inf.type.group.fbd.IOutVariable;
import plcopen.inf.type.group.fbd.IOutVariableInBlock;
import plcopen.inf.type.group.fbd.IReturn;
import plcopen.inf.type.group.fbd.IVariableInBlock;
import plcopen.inf.type.group.ld.ICoil;
import plcopen.inf.type.group.ld.IContact;
import plcopen.inf.type.group.ld.ILDObject;
import plcopen.inf.type.group.ld.ILeftPowerRail;
import plcopen.inf.type.group.ld.IRightPowerRail;
import plcopen.inf.type.group.sfc.ICondition;
import plcopen.inf.type.group.sfc.IConnectionList;
import plcopen.inf.type.group.sfc.IInline;
import plcopen.inf.type.group.sfc.IJumpStep;
import plcopen.inf.type.group.sfc.IMacroStep;
import plcopen.inf.type.group.sfc.IReference;
import plcopen.inf.type.group.sfc.ISFCObject;
import plcopen.inf.type.group.sfc.ISelectiveConvergence;
import plcopen.inf.type.group.sfc.ISelectiveDivergence;
import plcopen.inf.type.group.sfc.ISimultaneousConvergence;
import plcopen.inf.type.group.sfc.ISimultaneousDivergence;
import plcopen.inf.type.group.sfc.IStep;
import plcopen.inf.type.group.sfc.ITransition;
import plcopen.model.POUInterface;
import plcopen.type.Position;

/**
 * 
 * 
 * @author swkim
 * 
 */
public class XMLWriter {
	/**
	 * 
	 * 
	 * @param block
	 *            an object implementing IBlock (@see IBlock)
	 * @return a string for Block's attribute of FBDObject
	 */
	public static String getAttibutesString(IBlock block) {
		StringBuffer buf = new StringBuffer();
		buf.append(IBlock.ID_LOCALID + "=\"" + block.getLocalID() + "\" ");
		if (block.getSize() != null) {
			buf.append(IBlock.ID_WIDTH + "=\"" + block.getSize().getWidth()
					+ "\" ");
			buf.append(IBlock.ID_HEIGHT + "=\"" + block.getSize().getHeight()
					+ "\" ");
		}
		buf.append(IBlock.ID_TYPENAME + "=\"" + block.getTypeName() + "\" ");
		if (block.getInstanceName() != null && block.getInstanceName() != "")
			buf.append(IBlock.ID_INSTANCENAME + "=\"" + block.getInstanceName()
					+ "\" ");
		if (block.getExecutionOrderID() > 0)
			buf.append(IBlock.ID_EXECUTIONID + "=\""
					+ block.getExecutionOrderID() + "\" ");

		return buf.toString();
	}

	/**
	 * 
	 * 
	 * @param condition
	 *            an object implementing ICondition (@see ICondition)
	 * @return a string for Condition's attribute of Transition of SFCObject
	 */
	public static String getAttibutesString(ICondition condition) {
		StringBuffer buf = new StringBuffer();

		if (condition.isNegated())
			buf.append(ICondition.ID_NEGATED + "=\"" + true + "\" ");

		return buf.toString();
	}

	/**
	 * 
	 * 
	 * @param connection
	 *            an object implementing IConnection (@see IConnection)
	 * @return a string for Connetionn's attribute
	 */
	public static String getAttibutesString(IConnection connection) {
		StringBuffer buf = new StringBuffer();
		buf.append(IConnection.ID_REFLOCALID + "=\""
				+ connection.getRefLocalID() + "\" ");

		if (connection.getFormalParam() != "")
			buf.append(IConnection.ID_FORMALPARAM + "=\""
					+ connection.getFormalParam() + "\" ");

		return buf.toString();
	}

	public static String getAttibutesString(IVariableInBlock var) {
		StringBuffer buf = new StringBuffer();
		buf.append(IVariableInBlock.ID_FORMALPARAM + "=\""
				+ var.getFormalParameter() + "\" ");
		if (var.isNegated()) {
			buf.append(IVariableInBlock.ID_NEGATED + "=\"" + var.isNegated()
					+ "\" ");
		}
		if (var.getEdge().getType() != EdgeModifierType.NONE) {
			buf.append(IVariableInBlock.ID_EDGE + "=\""
					+ var.getEdge().getTypeStr() + "\" ");
		}
		if (var.getStorage().getType() != StorageModifierType.NONE) {
			buf.append(IVariableInBlock.ID_STORAGE + "=\""
					+ var.getStorage().getTypeStr() + "\" ");
		}
		if (var.isHidden()) {
			buf.append(IVariableInBlock.ID_HIDDEN + "=\"" + var.isHidden()
					+ "\" ");
		}
		return buf.toString();
	}

	public static String getAttibutesString(IVariableList vList) {
		if (vList.isPlain())
			return "";

		StringBuffer buf = new StringBuffer();
		buf.append(IVariableList.ID_NAME + "=\"" + vList.getName() + "\" ");
		buf.append(IVariableList.ID_CONSTANT + "=\"" + vList.isConstant()
				+ "\" ");
		buf.append(IVariableList.ID_RETAIN + "=\"" + vList.isRetain() + "\" ");
		buf.append(IVariableList.ID_NONRETAIN + "=\"" + vList.isNonRetain()
				+ "\" ");
		buf.append(IVariableList.ID_PERSISTENT + "=\"" + vList.isPersistent()
				+ "\" ");
		buf.append(IVariableList.ID_NONPERSISTENT + "=\""
				+ vList.isNonPersistent() + "\" ");

		return buf.toString();
	}

	public static String getAttributeString(IContentHeader contentHeader) {
		StringBuffer buf = new StringBuffer();

		String value = contentHeader.getName();
		buf.append(IContentHeader.ID_NAME + "=\"" + value + "\" ");

		value = contentHeader.getAuthor();
		if (value != "")
			buf.append(IContentHeader.ID_AUTHOR + "=\"" + value + "\" ");

		DateTime date = contentHeader.getDate();
		if (date == null)
			value = DateTime.now().toString();
		else
			value = date.toString();

		if (value != "")
			buf.append(IContentHeader.ID_DATE + "=\"" + value + "\" ");

		value = contentHeader.getLanguage();
		if (value != "")
			buf.append(IContentHeader.ID_LANGUAGE + "=\"" + value + "\" ");

		value = contentHeader.getOrganization();
		if (value != "")
			buf.append(IContentHeader.ID_ORGANIZATION + "=\"" + value + "\" ");

		value = contentHeader.getVersion();
		if (value != "")
			buf.append(IContentHeader.ID_VERSION + "=\"" + value + "\" ");

		return buf.toString();
	}

	/**
	 * 
	 * @return a string having all attributes of FileHeader in PLC Open
	 */
	public static String getAttributeString(IFileHeader fileHeader) {
		StringBuffer buf = new StringBuffer();

		String value = fileHeader.getCompanyName();
		buf.append(IFileHeader.ID_COM_NAME + "=\"" + value + "\" ");

		value = fileHeader.getCompanyURL();
		if (value != "")
			buf.append(IFileHeader.ID_COM_URL + "=\"" + value + "\" ");

		value = fileHeader.getProductName();
		buf.append(IFileHeader.ID_PRO_NAME + "=\"" + value + "\" ");

		value = fileHeader.getProductRelease();
		if (value != "")
			buf.append(IFileHeader.ID_PRO_REL + "=\"" + value + "\" ");

		value = fileHeader.getProductVersion();
		buf.append(IFileHeader.ID_PRO_VER + "=\"" + value + "\" ");

		value = fileHeader.getCreationDateTime().toString();
		buf.append(IFileHeader.ID_DATE + "=\"" + value + "\" ");

		value = fileHeader.getContentDescription();
		if (value != "")
			buf.append(IFileHeader.ID_DESCRIPTION + "=\"" + value + "\" ");

		return buf.toString();
	}

	static public String getFBDAttibutesString(IFBDObject object, String type) {
		StringBuffer buf = new StringBuffer();

		buf.append(IFBDObject.ID_LOCALID + "=\"" + object.getLocalID() + "\" ");
		if (object.getSize() != null) {
			buf.append(IFBDObject.ID_WIDTH + "=\""
					+ object.getSize().getWidth() + "\" ");
			buf.append(IFBDObject.ID_HEIGHT + "=\""
					+ object.getSize().getHeight() + "\" ");
		}

		if (IInVariable.ID_INVARIABLE.equals(type)
				|| IOutVariable.ID_OUTVARIABLE.equals(type)) {
			if (((IInVariable) object).isNegated()) {
				buf.append(IFBDObject.ID_NEGATED + "=\""
						+ ((IInVariable) object).isNegated() + "\" ");
			}
		}
		if (IInOutVariable.ID_INOUTVARIABLE.equals(type)) {
			if (((IInOutVariable) object).isNegatedIn()) {
				buf.append(IInOutVariable.ID_NEGATEDIN + "=\""
						+ ((IInOutVariable) object).isNegatedIn() + "\" ");
			}
			if (((IInOutVariable) object).isNegatedOut()) {

				buf.append(IInOutVariable.ID_NEGATEDOUT + "=\""
						+ ((IInOutVariable) object).isNegatedOut() + "\" ");
			}
		}

		if (IInVariable.ID_INVARIABLE.equals(type)
				|| IOutVariable.ID_OUTVARIABLE.equals(type)) {
			if (((IInVariable) object).getEdge().getType() != EdgeModifierType.NONE) {
				buf
						.append(IOutVariable.ID_EDGE + "=\""
								+ ((IInVariable) object).getEdge().getTypeStr()
								+ "\" ");
			}
			if (((IInVariable) object).getStorage().getType() != StorageModifierType.NONE) {
				buf.append(IInVariable.ID_STORAGE + "=\""
						+ ((IInVariable) object).getStorage().getTypeStr()
						+ "\" ");
			}
		}
		if (IInOutVariable.ID_INOUTVARIABLE.equals(type)) {
			if (((IInOutVariable) object).getEdgeIn().getType() != EdgeModifierType.NONE) {
				buf.append(IInOutVariable.ID_EDGEIN + "=\""
						+ ((IInOutVariable) object).getEdgeIn().getTypeStr()
						+ "\" ");
			}
			if (((IInOutVariable) object).getEdgeOut().getType() != EdgeModifierType.NONE) {
				buf.append(IInOutVariable.ID_EDGEOUT + "=\""
						+ ((IInOutVariable) object).getEdgeOut().getTypeStr()
						+ "\" ");
			}
			if (((IInOutVariable) object).getStorageIn().getType() != StorageModifierType.NONE) {
				buf.append(IInOutVariable.ID_STORAGEIN + "=\""
						+ ((IInOutVariable) object).getStorageIn().getTypeStr()
						+ "\" ");
			}
			if (((IInOutVariable) object).getStorageOut().getType() != StorageModifierType.NONE) {
				buf.append(IInOutVariable.ID_STORAGEOUT
						+ "=\""
						+ ((IInOutVariable) object).getStorageOut()
								.getTypeStr() + "\" ");
			}
		}

		if (object.getExecutionOrderID() > 0) {
			buf.append(IFBDObject.ID_EXECUTIONID + "=\""
					+ object.getExecutionOrderID() + "\" ");
		}

		if (IJump.ID_JUMP.equals(type) || ILabel.ID_LABEL.equals(type)) {
			buf.append(ILabel.ID_LABEL + "=\"" + ((ILabel) object).getLable()
					+ "\" ");
		}

		return buf.toString();
	}

	private static String getLDAttibutesString(ILDObject object, String type) {
		StringBuffer buf = new StringBuffer();

		// Common attribute
		buf.append(ILDObject.ID_LOCALID + "=\"" + object.getLocalID() + "\" ");
		if (object.getSize() != null) {
			buf.append(ILDObject.ID_WIDTH + "=\"" + object.getSize().getWidth()
					+ "\" ");
			buf.append(ILDObject.ID_HEIGHT + "=\""
					+ object.getSize().getHeight() + "\" ");
		}

		if (ILDObject.ID_COIL.equals(type)) {
			ICoil coil = (ICoil) object;
			buf.append(ILDObject.ID_EXECUTIONID + "=\""
					+ coil.getExecutionOrderID() + "\" ");
			if (coil.isNegated())
				buf.append(ILDObject.ID_NEGATED + "=\"" + true + "\" ");
			buf.append(ILDObject.ID_STORAGE + "=\""
					+ coil.getStorage().getTypeStr() + "\" ");
			buf.append(ILDObject.ID_EDGE + "=\"" + coil.getEdge().getTypeStr()
					+ "\" ");
		}

		if (ILDObject.ID_CONTACT.equals(type)) {
			IContact contact = (IContact) object;
			buf.append(ILDObject.ID_EXECUTIONID + "=\""
					+ contact.getExecutionOrderID() + "\" ");
			if (contact.isNegated())
				buf.append(ILDObject.ID_NEGATED + "=\"" + true + "\" ");
			buf.append(ILDObject.ID_STORAGE + "=\""
					+ contact.getStorage().getTypeStr() + "\" ");
			buf.append(ILDObject.ID_EDGE + "=\""
					+ contact.getEdge().getTypeStr() + "\" ");
		}
		return buf.toString();
	}

	public static String getSFCAttibutesString(ISFCObject obj, String type) {
		StringBuffer buf = new StringBuffer();

		// localID attribute
		buf.append(IBlock.ID_LOCALID + "=\"" + obj.getLocalID() + "\" ");
		// size attribute
		if (obj.getSize() != null) {
			buf.append(ISFCObject.ID_WIDTH + "=\"" + obj.getSize().getWidth()
					+ "\" ");
			buf.append(ISFCObject.ID_HEIGHT + "=\"" + obj.getSize().getHeight()
					+ "\" ");
		}

		// for Step
		if (IStep.ID_STEP.equals(type)) {
			buf.append(IStep.ID_NAME + "=\"" + ((IStep) obj).getName() + "\" ");

			// negated attribute
			if (((IStep) obj).isNegated()) {
				buf.append(IStep.ID_NEGATED + "=\"" + true + "\" ");
			}

			// initialStep attribute
			if (((IStep) obj).isInitialStep()) {
				buf.append(IStep.ID_INITSTEP + "=\"" + true + "\" ");
			}
		}
		// for transition
		if (ISFCObject.ID_TRANSITION.equals(type)) {
			// Attribute - Priority -
			long priority = ((ITransition) obj).getPriority();
			if (priority >= 0) {
				buf.append(ITransition.ID_PRIORITY + "=\""
						+ Long.toString(priority) + "\" ");
			}
		}

		// Name Attribute except for Step
		String name = "";
		if (ISFCObject.ID_MARCOSTEP.equals(type))
			name = ((IMacroStep) obj).getName();
		else if (ISFCObject.ID_SIMULDIV.equals(type))

			name = ((ISimultaneousDivergence) obj).getName();

		if (name != null && name != "") {
			buf.append(ISimultaneousDivergence.ID_NAME + "=\"" + name + "\" ");
		}

		if (ISFCObject.ID_JUMPSTEP.equals(type)) {
			buf.append(IJumpStep.ID_TARGETNAME + "=\""
					+ ((IJumpStep) obj).getTargetName() + "\" ");
		}

		return buf.toString();
	}

	public static void writeFBDBodyXMLContents(IFBD fbd, XMLPrintStream os) {
		// Wrting Common Objects
		for (IComment comment : fbd.getComments()) {
			writeXMLContents(comment, os);
		}
		for (IError error : fbd.getErrors()) {
			writeXMLContents(error, os);
		}
		for (IConnector connector : fbd.getConnectors()) {
			writeXMLContents(connector, os);
		}
		for (IContinuation continuation : fbd.getContinuations()) {
			writeXMLContents(continuation, os);
		}
		for (IActionBlock aBlock : fbd.getActionBlks()) {
			writeXMLContents(aBlock, os);
		}

		// Writing FBDObjects
		for (IBlock block : fbd.getBlocks()) {
			writeXMLContents(block, os);
		}
		for (IInVariable inVar : fbd.getInVariables()) {
			writeXMLContents(inVar, os, IInVariable.ID_INVARIABLE);
		}
		for (IOutVariable outVar : fbd.getOutVariables()) {
			writeXMLContents(outVar, os, IOutVariable.ID_OUTVARIABLE);
		}
		for (IInOutVariable inoutVar : fbd.getInOutVariables()) {
			writeXMLContents(inoutVar, os, IInOutVariable.ID_INOUTVARIABLE);
		}
		for (ILabel label : fbd.getLabels()) {
			writeXMLContents(label, os, ILabel.ID_LABEL);
		}
		for (IJump jump : fbd.getJumps()) {
			writeXMLContents(jump, os, IJump.ID_JUMP);
		}
		for (IReturn ret : fbd.getReturns()) {
			writeXMLContents(ret, os, IReturn.ID_RETURN);
		}
	}

	public static void writeLDBodyXMLContents(ILD body, XMLPrintStream os) {
		for (ICoil coil : body.getCoils()) {
			writeXMLContents(coil, os, ILDObject.ID_COIL);
		}
		for (IContact contact : body.getContacts()) {
			writeXMLContents(contact, os, ILDObject.ID_CONTACT);
		}
		for (ILeftPowerRail rail : body.getLeftPowerRail()) {
			writeXMLContents(rail, os, ILDObject.ID_LEFTRAIL);
		}
		for (IRightPowerRail rail : body.getRightPowerRail()) {
			writeXMLContents(rail, os, ILDObject.ID_RIGHTRAIL);
		}

		writeFBDBodyXMLContents(body, os);
	}

	public static void writeSFCBodyXMLContents(ISFC body, XMLPrintStream os) {
		for (IStep step : body.getSteps()) {
			writeXMLContents(step, os, ISFCObject.ID_STEP);
		}
		for (IMacroStep mStep : body.getMacroSteps()) {
			writeXMLContents(mStep, os, ISFCObject.ID_MARCOSTEP);
		}
		for (IJumpStep jStep : body.getJumpSteps()) {
			writeXMLContents(jStep, os, ISFCObject.ID_JUMPSTEP);
		}
		for (ITransition tran : body.getTransitions()) {
			writeXMLContents(tran, os, ISFCObject.ID_TRANSITION);
		}
		for (ISelectiveConvergence selCon : body.getSelectionConvergence()) {
			writeXMLContents(selCon, os, ISFCObject.ID_SELCON);
		}
		for (ISimultaneousConvergence simulCon : body
				.getSimultaneousConvergence()) {
			writeXMLContents(simulCon, os, ISFCObject.ID_SIMULCON);
		}
		for (ISelectiveDivergence selDiv : body.getSelectionDivergence()) {
			writeXMLContents(selDiv, os, ISFCObject.ID_SELDIV);
		}
		for (ISimultaneousDivergence simulDiv : body
				.getSimultaneousDivergence()) {
			writeXMLContents(simulDiv, os, ISFCObject.ID_SIMULDIV);
		}

		writeLDBodyXMLContents(body, os);
	}

	/**
	 * 
	 * 
	 * @param project
	 * @param file
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void writeToXML(IPLCProject project, File file)
			throws ParserConfigurationException, IOException {
		XMLPrintStream os = new XMLPrintStream();
		XMLWriter.writeXMLContents(project, os);

		FileOutputStream fos = new FileOutputStream(file);
		fos.write(os.getByte(true));
		fos.flush();
		fos.close();
	}

	// DONE
	private static void writeValueSubXMLContents(IArrayValueType value,
			XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - repetitionValue - required
		if (value.getRepetitionValue() != 1)
			buf.append(IArrayValueType.ID_REPETITIONVALUE + "=\""
					+ value.getRepetitionValue() + "\" ");

		/* Start Tag */
		os
				.println("<" + IArrayValueType.ID_VALUE + " " + buf.toString()
						+ " >");
		os.increaseTab();

		/* Element Generation */
		// Element - base type - [1]
		writeXMLContents(value, os);

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IArrayValueType.ID_VALUE + " >");
	}

	// Done
	private static void writeValueSubXMLContents(IStructValueType value,
			XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - repetitionValue - required
		buf.append(IStructValueType.ID_MEMBER + "=\"" + value.getMember()
				+ "\" ");

		/* Start Tag */
		os.println("<" + IStructValueType.ID_VALUE + " " + buf.toString()
				+ " >");
		os.increaseTab();

		/* Element Generation */
		// Element - base type - [1]
		writeXMLContents(value, os);

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IStructValueType.ID_VALUE + " >");
	}

	private static void writeXMLContents(IAction action, XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - qualifier - optional
		buf.append(IAction.ID_QUALIFIER + "=\""
				+ action.getQualifier().toString() + "\" ");
		// Attribute - duration - optional
		buf.append(IAction.ID_DURATION + "=\"" + action.getDuration() + "\" ");
		// Attribute - indicator - optional
		buf
				.append(IAction.ID_INDICATOR + "=\"" + action.getIndicator()
						+ "\" ");

		/* Start Tag */
		os.println("<" + IAction.ID_ACTION + " " + buf.toString() + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Reference - [0..1]
		IReference ref = action.getReference();
		if (ref != null) {
			os.println("<" + IAction.ID_REFERENCE + " " + IReference.ID_NAME
					+ "=\"" + ref.getName() + "\" />");
		}
		// Element - inline - [0..1]
		if (action.getInline() != null)
			writeXMLContents(action.getInline(), IAction.ID_INLINE, os);
		// Element - Documentation - [0..1]
		if (action.getDocumentation() != "") {
			os.println("<" + IAction.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(action.getDocumentation());
			os.decreaseTab();
			os.println("</" + IAction.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IAction.ID_ACTION + ">");
	}

	private static void writeXMLContents(IActionBlock block, XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - negated - required
		if (block.isNegated())
			buf.append(IActionBlock.ID_NEGATED + "=\"" + block.isNegated()
					+ "\" ");

		/* Start Tag */
		os
				.println("<" + IActionBlock.ID_ACTIONBLK + " " + buf.toString()
						+ ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Position - [1]
		writeXMLContents(block.getPosition(), os);
		// Element - ConnectionPointIn - [0..1]
		IConnectionPointIn cin = block.getConnectionPointIn();
		if (cin != null) {
			writeXMLContents(cin, os);
		}
		// Element - ConnectionPointOut - [0..1]
		IConnectionPointOut cout = block.getConnectionPointOut();
		if (cout != null) {
			writeXMLContents(cout, os, IConnectionPointOut.ID_CONOUT);
		}
		// Element - Action - [0..*]
		List<IAction> actions = block.getActions();
		for (IAction action : actions) {
			writeXMLContents(action, os);
		}
		// Element - Documentation - [0..1]
		if (block.getDocumentation() != "") {
			os.println("<" + IActionBlock.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(block.getDocumentation());
			os.decreaseTab();
			os.println("</" + IActionBlock.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IActionBlock.ID_ACTIONBLK + ">");
	}

	public static void writeXMLContents(IActionType action, XMLPrintStream os) {
		/* Attribute Generation */
		// Attribute - Name - Required
		StringBuffer buf = new StringBuffer();
		buf.append(IActionType.ID_NAME + "=\"" + action.getName() + "\" ");

		/* Start Tag */
		os.println("<" + IActionType.ID_ACTION + " " + buf.toString() + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Body - [1]
		writeXMLContents(action.getBody(), IBody.ID_BODY, os);
		// Element - Documentation - [0..1]
		if (action.getDocumentation() != "") {
			os.println("<" + IActionType.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(action.getDocumentation());
			os.decreaseTab();
			os.println("</" + IActionType.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IActionType.ID_ACTION + ">");
	}

	public static void writeXMLContents(IBlock block, XMLPrintStream os) {
		/* Attribute Generation */
		String attributes = getAttibutesString(block);

		/* Start Tag */
		os.println("<" + IBlock.ID_BLOCK + " " + attributes + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Position - [1]
		writeXMLContents(block.getPosition(), os);
		// Element - InVariables - [1]
		if (block.getInVariables().size() == 0) {
			os.println("<" + IBlock.ID_INVARIABLES + " />");
		} else {
			os.println("<" + IBlock.ID_INVARIABLES + " >");
			os.increaseTab();
			for (IInVariableInBlock var : block.getInVariables()) {
				writeXMLContents(var, os, IInVariableInBlock.ID_INVARIABLE);
			}
			os.decreaseTab();
			os.println("</" + IBlock.ID_INVARIABLES + " >");
		}
		// Element - OutVariables - [1]
		if (block.getOutVariables().size() == 0) {
			os.println("<" + IBlock.ID_OUTVARIABLES + " />");
		} else {
			os.println("<" + IBlock.ID_OUTVARIABLES + " >");
			os.increaseTab();
			for (IOutVariableInBlock var : block.getOutVariables()) {
				writeXMLContents(var, os, IOutVariableInBlock.ID_OUTVARIABLE);
			}
			os.decreaseTab();
			os.println("</" + IBlock.ID_OUTVARIABLES + " >");
		}
		// Element - InOutVariables - [1]
		if (block.getInOutVariables().size() == 0) {
			os.println("<" + IBlock.ID_INOUTVARIABLES + " />");
		} else {
			os.println("<" + IBlock.ID_INOUTVARIABLES + " >");
			os.increaseTab();
			for (IInOutVariableInBlock var : block.getInOutVariables()) {
				writeXMLContents(var, os,
						IInOutVariableInBlock.ID_INOUTVARIABLE);
			}
			os.decreaseTab();
			os.println("</" + IBlock.ID_INOUTVARIABLES + " >");
		}
		// Element - Documentation - [0..1]
		if (block.getDocumentation() != null && block.getDocumentation() != "") {
			os.println("<" + IBlock.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(block.getDocumentation());
			os.decreaseTab();
			os.println("</" + IBlock.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IBlock.ID_BLOCK + ">");
	}

	public static void writeXMLContents(IBody body, String titleID,
			XMLPrintStream os) {
		/* Start Tag */
		os.println("<" + titleID + ">");
		os.increaseTab();

		/* Element Generation */
		if (body instanceof ISFC) {
			os.println("<" + IBody.ID_SFC + ">");
			os.increaseTab();
			writeSFCBodyXMLContents((ISFC) body, os);
			os.decreaseTab();
			os.println("</" + IBody.ID_SFC + ">");
		} else if (body instanceof ILD) {
			os.println("<" + IBody.ID_LD + ">");
			os.increaseTab();
			writeLDBodyXMLContents((ILD) body, os);
			os.decreaseTab();
			os.println("</" + IBody.ID_LD + ">");
		} else if (body instanceof IFBD) {
			os.println("<" + IBody.ID_FBD + ">");
			os.increaseTab();
			writeFBDBodyXMLContents((IFBD) body, os);
			os.decreaseTab();
			os.println("</" + IBody.ID_FBD + ">");
		} else if (body instanceof IST) {
			os.println("<" + IBody.ID_ST + ">");
			os.increaseTab();
			os.println(((IIL) body).getContents());
			os.decreaseTab();
			os.println("</" + IBody.ID_ST + ">");
		} else if (body instanceof IIL) {
			os.println("<" + IBody.ID_IL + ">");
			os.increaseTab();
			os.println(((IST) body).getContents());
			os.decreaseTab();
			os.println("</" + IBody.ID_IL + ">");
		}

		if (body.getDocumentation() != "") {
			os.println("<" + IBody.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(body.getDocumentation());
			os.decreaseTab();
			os.println("</" + IBody.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + titleID + ">");
	}

	private static void writeXMLContents(IComment comment, XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - localID - Required
		buf.append(IComment.ID_LOCALID + "=\""
				+ Long.toString(comment.getLocalID()) + "\" ");
		// Attribute - height - required
		buf.append(IComment.ID_HEIGHT + "=\""
				+ Integer.toString(comment.getSize().getHeight()) + "\" ");
		// Attribute - width - required
		buf.append(IComment.ID_WIDTH + "=\""
				+ Integer.toString(comment.getSize().getWidth()) + "\" ");

		/* Start Tag */
		os.println("<" + IComment.ID_COMMENT + " " + buf.toString() + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Position - [1]
		writeXMLContents(comment.getPosition(), os);
		// Element - Content - [1]
		os.println("<" + IComment.ID_CONTENT + ">");
		os.increaseTab();
		os.println(comment.getContent());
		os.decreaseTab();
		os.println("</" + IComment.ID_CONTENT + ">");
		// Element - Documentation - [0..1]
		if (comment.getDocumentation() != "") {
			os.println("<" + IComment.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(comment.getDocumentation());
			os.decreaseTab();
			os.println("</" + IComment.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IComment.ID_COMMENT + ">");
	}

	public static void writeXMLContents(ICondition condition, XMLPrintStream os) {

		if (condition.getType().equalsIgnoreCase(""))
			return;

		String attribute = getAttibutesString(condition);
		os.println("<" + ITransition.ID_CONDITION + " " + attribute + ">");
		os.increaseTab();

		if (condition.getType() == ICondition.ID_REFERENCE) {
			StringBuffer buf = new StringBuffer();
			buf.append(ICondition.ID_NAME + "=\""
					+ ((IReference) condition).getName() + "\" ");
			os.println("<" + ICondition.ID_REFERENCE + " " + buf.toString()
					+ "/>");
		} else if (condition.getType() == ICondition.ID_CONLIST) {
			for (IConnection con : ((IConnectionList) condition)
					.getConnections()) {
				writeXMLContents(con, os);
			}
		} else if (condition.getType() == ICondition.ID_INLINE) {
			IInline inline = (IInline) condition;
			StringBuffer buf = new StringBuffer();
			buf.append(ICondition.ID_NAME + "=\"" + inline.getName() + "\" ");
			os.println("<" + ICondition.ID_INLINE + " " + buf.toString() + ">");
			os.increaseTab();
			writeXMLContents(inline.getBody(), ICondition.ID_INLINE, os);
			os.decreaseTab();
			os.println("</" + ICondition.ID_INLINE + ">");
		}

		os.decreaseTab();
		os.println("</" + ITransition.ID_CONDITION + ">");
	}

	// DONE
	public static void writeXMLContents(IConfiguration conf, XMLPrintStream os) {
		/* Attribute Generation */
		// Attribute - Name - Required
		StringBuffer buf = new StringBuffer();
		buf.append(IConfiguration.ID_NAME + "=\"" + conf.getName() + "\" ");

		/* Start Tag */
		os.println("<" + IConfiguration.ID_CONFIGURATION + " " + buf.toString()
				+ ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Resource [0..*]
		List<IResource> res = conf.getResources();
		for (IResource resource : res) {
			writeXMLContents(resource, os);
		}
		// Element - Global Vars [0..*]
		List<IVariableList> vars = conf.getGlobalVars();
		for (IVariableList var : vars) {
			writeXMLContents(var, IConfiguration.ID_GLOBALVARS, false, os);
		}
		// Element - Documentation - [0..1]
		if (conf.getDocumentation() != "") {
			os.println("<" + IConfiguration.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(conf.getDocumentation());
			os.decreaseTab();
			os.println("</" + IConfiguration.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IConfiguration.ID_CONFIGURATION + ">");
	}

	public static void writeXMLContents(IConnection connection,
			XMLPrintStream os) {
		String attribute = getAttibutesString(connection);

		List<IPosition> positions = connection.getPositions();

		if (positions.size() > 0) {
			os.println("<" + IConnection.ID_CONNECTION + " " + attribute + ">");
			os.increaseTab();
			for (IPosition position : positions) {
				writeXMLContents(position, os);
			}
			os.decreaseTab();

			os.println("</" + IConnection.ID_CONNECTION + ">");
		} else {
			os
					.println("<" + IConnection.ID_CONNECTION + " " + attribute
							+ "/>");
		}
	}

	public static void writeXMLContents(IConnectionPointIn in, XMLPrintStream os) {
		if (!in.hasConnections() && !in.hasExpression())
			return;

		os.println("<" + IConnectionPointIn.ID_CONIN + " >");
		os.increaseTab();
		if (in.hasRelativePosition()) {
			writeXMLContents(in.getRelativePosition(), os,
					IConnectionPointIn.ID_RELPOSITION);
		}

		if (in.hasExpression() && in.hasConnections())
			System.err.println("Illegal ConnectionPointIn object");

		if (in.hasExpression()) {
			os.println("<" + IConnectionPointIn.ID_EXPRESSION + ">");
			os.increaseTab();
			os.println(in.getExpression());
			os.decreaseTab();
			os.println("</" + IConnectionPointIn.ID_EXPRESSION + ">");
		} else if (in.hasConnections()) {
			if (in.getConnections().size() < 1)
				System.err
						.println("Illegal connection in ConnectionPointIn object");

			for (IConnection connection : in.getConnections()) {
				writeXMLContents(connection, os);
			}
		}

		os.decreaseTab();
		os.println("</" + IConnectionPointIn.ID_CONIN + " >");
	}

	/**
	 * 
	 * @param out
	 * @param os
	 * @param type
	 *            {@link IConnectionPointOut#ID_CONOUTACTION}
	 *            {@link IConnectionPointOut#ID_CONOUTWITHPARAM},
	 *            {@link IConnectionPointOut#ID_CONOUT}
	 */
	public static void writeXMLContents(IConnectionPointOut out,
			XMLPrintStream os, String type) {
		String id = IConnectionPointOut.ID_CONOUT;
		if (type.equals(IConnectionPointOut.ID_CONOUTACTION))
			id = IConnectionPointOut.ID_CONOUTACTION;

		String attribute = "";
		if (IConnectionPointOut.ID_CONOUTWITHPARAM.equals(type)
				|| IConnectionPointOut.ID_CONOUTACTION.equals(type))
			attribute = IConnectionPointOut.ID_FORMALPARAM + "=\""
					+ out.getFormalParameter() + "\" ";

		os.println("<" + id + " " + attribute + ">");
		os.increaseTab();

		if (!out.isIgnoreRelativePosition()) {
			// if (!out.isIgnoreExpression()
			// || (out.getFormalParameter() != null && !out
			// .getFormalParameter().equals("")))
			writeXMLContents(out.getRelativePosition(), os,
					IConnectionPointOut.ID_RELPOSITION);
		}
		if (!out.isIgnoreExpression()) {
			os.println("<" + IConnectionPointOut.ID_EXPRESSION + ">");
			os.increaseTab();
			os.println(out.getExpression());
			os.decreaseTab();
			os.println("</" + IConnectionPointOut.ID_EXPRESSION + ">");
		}
		os.decreaseTab();
		os.println("</" + id + ">");

	}

	private static void writeXMLContents(IConnector connector, XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - localID - Required
		buf.append(IConnector.ID_LOCALID + "=\""
				+ Long.toString(connector.getLocalID()) + "\" ");
		// Attribute - name - required
		buf.append(IConnector.ID_NAME + "=\"" + connector.getName() + "\" ");
		// Attribute - height - optional
		buf.append(IConnector.ID_HEIGHT + "=\""
				+ Integer.toString(connector.getSize().getHeight()) + "\" ");
		// Attribute - width - optional
		buf.append(IConnector.ID_WIDTH + "=\""
				+ Integer.toString(connector.getSize().getWidth()) + "\" ");

		/* Start Tag */
		os.println("<" + IConnector.ID_CONNECTOR + " " + buf.toString() + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Position - [1]
		writeXMLContents(connector.getPosition(), os);
		// Element - ConnectionPointIn - [0..1]
		IConnectionPointIn cin = connector.getConnectionPointIn();
		if (cin != null) {
			writeXMLContents(cin, os);
		}
		// Element - Documentation - [0..1]
		if (connector.getDocumentation() != "") {
			os.println("<" + IConnector.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(connector.getDocumentation());
			os.decreaseTab();
			os.println("</" + IConnector.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IConnector.ID_CONNECTOR + ">");

	}

	public static void writeXMLContents(IContentHeader contentHeader,
			XMLPrintStream os) {
		String attribute = getAttributeString(contentHeader);

		os.println("<" + IContentHeader.ID_CONTENTHEADER + " " + attribute
				+ ">");
		os.increaseTab();

		String value = contentHeader.getComment();
		if (value != null && value != "") {
			os.println("<Comment>");
			os.increaseTab();
			os.println(value);
			os.decreaseTab();
			os.println("</Comment>");
		}

		writeXMLContents(contentHeader.getCoordinationInfo(), os);

		os.decreaseTab();
		os.println("</contentHeader>");

	}

	private static void writeXMLContents(IContinuation continuation,
			XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - localID - Required
		buf.append(IContinuation.ID_LOCALID + "=\""
				+ Long.toString(continuation.getLocalID()) + "\" ");
		// Attribute - name - required
		buf.append(IContinuation.ID_NAME + "=\"" + continuation.getName()
				+ "\" ");
		// Attribute - height - optional
		buf.append(IContinuation.ID_HEIGHT + "=\""
				+ Integer.toString(continuation.getSize().getHeight()) + "\" ");
		// Attribute - width - optional
		buf.append(IContinuation.ID_WIDTH + "=\""
				+ Integer.toString(continuation.getSize().getWidth()) + "\" ");

		/* Start Tag */
		os.println("<" + IContinuation.ID_CONTINUATION + " " + buf.toString()
				+ ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Position - [1]
		writeXMLContents(continuation.getPosition(), os);
		// Element - ConnectionPointOut - [0..1]
		IConnectionPointOut cout = continuation.getConnectionPointOut();
		if (cout != null) {
			writeXMLContents(cout, os, IConnectionPointOut.ID_CONOUT);
		}
		// Element - Documentation - [0..1]
		if (continuation.getDocumentation() != "") {
			os.println("<" + IContinuation.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(continuation.getDocumentation());
			os.decreaseTab();
			os.println("</" + IContinuation.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IContinuation.ID_CONTINUATION + ">");
	}

	public static void writeXMLContents(ICoordinationInfo info,
			XMLPrintStream os) {

		os.println("<coordinateInfo>");
		os.increaseTab();
		IPosition size = info.getPageSize();
		if (size != null && !size.equals(new Position(0, 0))) {
			writeXMLContents(size, os, ICoordinationInfo.ID_PAGESIZE);
		}

		IPosition scaling = info.getFBDScaling();
		os.println("<" + ICoordinationInfo.ID_FBDSCALING + ">");
		os.increaseTab();
		writeXMLContents(scaling, os, ICoordinationInfo.ID_SCALING);
		os.decreaseTab();
		os.println("</" + ICoordinationInfo.ID_FBDSCALING + ">");

		scaling = info.getLDScaling();
		os.println("<" + ICoordinationInfo.ID_LDSCALING + ">");
		os.increaseTab();
		writeXMLContents(scaling, os, ICoordinationInfo.ID_SCALING);
		os.decreaseTab();
		os.println("</" + ICoordinationInfo.ID_LDSCALING + ">");

		scaling = info.getSFCScaling();
		os.println("<" + ICoordinationInfo.ID_SFCSCALING + ">");
		os.increaseTab();
		writeXMLContents(scaling, os, ICoordinationInfo.ID_SCALING);
		os.decreaseTab();
		os.println("</" + ICoordinationInfo.ID_SFCSCALING + ">");
		os.decreaseTab();
		os.println("</coordinateInfo>");

	}

	/**
	 * 
	 * @param type
	 * @param os
	 */
	public static void writeXMLContents(IDataType type, XMLPrintStream os) {
		if (type instanceof IElementaryType) {
			IElementaryType eType = (IElementaryType) type;
			String name = eType.getTypeName();
			if (IElementaryType.ID_STRING.equals(name)
					|| IElementaryType.ID_WSTRING.equals(name)) {
				os.println("<" + name + " " + IElementaryType.ID_LENGTH + "=\""
						+ eType.getLength() + "\" />");
			} else
				os.println("<" + name + "/>");
		} else if (type instanceof IDerivedType) {
			if (type instanceof IArray) {
				os.println("<" + IArray.ID_ARRAY + ">");
				os.increaseTab();
				IArray array = (IArray) type;
				// Element - dimension - [1..*]
				List<IRange> dims = array.getDimensions();
				for (IRange dim : dims) {
					os.increaseTab();
					writeXMLContents(dim, IArray.ID_DIMENSION, os);
					os.decreaseTab();
				}
				// Element - BaseTYpe - [1]
				os.println("<" + IArray.ID_BASETYPE + ">");
				os.increaseTab();
				writeXMLContents(array.getBaseType(), os);
				os.decreaseTab();
				os.println("</" + IArray.ID_BASETYPE + ">");
				/* End Tag */
				os.decreaseTab();
				os.println("</" + IArray.ID_ARRAY + ">");
			} else if (type instanceof IDerived) {
				os.println("<" + IDerived.ID_DERIVED + " " + IDerived.ID_NAME
						+ "+\"" + ((IDerived) type).getName() + "/>");
			} else if (type instanceof IEnum) {
				/* Start Tag */
				os.println("<" + IEnum.ID_ENUM + ">");
				os.increaseTab();

				IEnum e = (IEnum) type;
				os.println("<" + IEnum.ID_VALUES + ">");
				os.increaseTab();
				List<IEnum.Pair> pairs = e.getValues();
				for (IEnum.Pair pair : pairs) {
					os
							.println("<"
									+ IEnum.ID_VALUE
									+ " "
									+ IEnum.ID_NAME
									+ "=\""
									+ pair.name
									+ "\""
									+ ((pair.value != null && pair.value != "") ? (IEnum.ID_VALUE
											+ "=\"" + pair.value + "\"")
											: "") + "/>");

				}
				os.decreaseTab();
				os.println("</" + IEnum.ID_VALUES + ">");
				// Element - basetype - [0..1]
				IDataType baseType = e.getBaseType();
				os.println("<" + IEnum.ID_BASETYPE + ">");
				os.increaseTab();
				writeXMLContents(baseType, os);
				os.decreaseTab();
				os.println("</" + IEnum.ID_BASETYPE + ">");
				/* End Tag */
				os.decreaseTab();
				os.println("</" + IEnum.ID_ENUM + ">");
			} else if (type instanceof IStruct) {
				writeXMLContents((IVariableList) type, IStruct.ID_STRUCT, true,
						os);
			} else if (type instanceof IRangeType) {
				IRangeType rType = (IRangeType) type;
				if (rType.isSigned()) {
					// subrangeSigned
					/* Start Tag */
					os.println("<" + IRangeType.ID_SUBRANGESIGNED + ">");
					os.increaseTab();
					// Element - Range - [1]
					IRange range = rType.getRange();
					writeXMLContents(range, IRangeType.ID_RANGE, os);
					// Element - basetype - [1]
					IDataType baseType = rType.getBaseType();
					os.println("<" + IRangeType.ID_BASETYPE + ">");
					os.increaseTab();
					writeXMLContents(baseType, os);
					os.decreaseTab();
					os.println("</" + IRangeType.ID_BASETYPE + ">");
					/* End Tag */
					os.decreaseTab();
					os.println("</" + IRangeType.ID_SUBRANGESIGNED + ">");
				} else {
					// subrangeUnsinged
					/* Start Tag */
					os.println("<" + IRangeType.ID_SUBRANGEUNSIGNED + ">");
					os.increaseTab();
					// Element - Range - [1]
					IRange range = rType.getRange();
					writeXMLContents(range, IRangeType.ID_RANGE, os);
					// Element - basetype - [1]
					IDataType baseType = rType.getBaseType();
					os.println("<" + IRangeType.ID_BASETYPE + ">");
					os.increaseTab();
					writeXMLContents(baseType, os);
					os.decreaseTab();
					os.println("</" + IRangeType.ID_BASETYPE + ">");
					/* End Tag */
					os.decreaseTab();
					os.println("</" + IRangeType.ID_SUBRANGEUNSIGNED + ">");
				}
			}
		} else if (type instanceof IExtendedType) {
			if (type instanceof IPointerType) {
				os.println("<" + IPointerType.ID_POINT + ">");
				os.increaseTab();
				os.println("<" + IPointerType.ID_BASETYPE + ">");
				writeXMLContents(((IPointerType) type).getBaseType(), os);
				os.println("</" + IPointerType.ID_BASETYPE + ">");
				os.decreaseTab();
				os.println("</" + IPointerType.ID_POINT + ">");
			}
		}
	}

	// DONE
	public static void writeXMLContents(IDataTypeType dataType,
			XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - name - Required
		buf.append(IDataTypeType.ID_NAME + "=\"" + dataType.getName() + "\" ");

		/* Start Tag */
		os
				.println("<" + IDataTypeType.ID_DATATYPE + " " + buf.toString()
						+ ">");
		os.increaseTab();

		/* Element Generation */
		// Element - BaseType - [1]
		os.println("<" + IDataTypeType.ID_BASETYPE + ">");
		os.increaseTab();
		writeXMLContents(dataType.getBaseType(), os);
		os.decreaseTab();
		os.println("</" + IDataTypeType.ID_BASETYPE + ">");
		// Element - InitialValue - [0..1]
		if (dataType.getInitialValue() != null) {
			os.println("<" + IDataTypeType.ID_INITIALVALUE + ">");
			os.increaseTab();
			writeXMLContents(dataType.getInitialValue(), os);
			os.decreaseTab();
			os.println("</" + IDataTypeType.ID_INITIALVALUE + ">");
		}
		// Element - Documentation - [0..1]
		if (dataType.getDocumentation() != "") {
			os.println("<" + IError.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(dataType.getDocumentation());
			os.decreaseTab();
			os.println("</" + IError.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IDataTypeType.ID_DATATYPE + ">");
	}

	public static void writeXMLContents(IElementaryType type, XMLPrintStream os) {
		StringBuffer buf = new StringBuffer();
		buf.append("<" + type.getTypeName() + " ");

		if (IElementaryType.ID_STRING.equals(type.getTypeName())
				|| IElementaryType.ID_WSTRING.equals(type.getTypeName())) {
			buf.append(IElementaryType.ID_LENGTH + "=\"" + type.getLength()
					+ "\" ");
		}
		buf.append("/>");
		os.println(buf.toString());
	}

	private static void writeXMLContents(IError error, XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - localID - Required
		buf.append(IError.ID_LOCALID + "=\""
				+ Long.toString(error.getLocalID()) + "\" ");
		// Attribute - height - required
		buf.append(IError.ID_HEIGHT + "=\""
				+ Integer.toString(error.getSize().getHeight()) + "\" ");
		// Attribute - width - required
		buf.append(IError.ID_WIDTH + "=\""
				+ Integer.toString(error.getSize().getWidth()) + "\" ");

		/* Start Tag */
		os.println("<" + IError.ID_ERROR + " " + buf.toString() + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Position - [1]
		writeXMLContents(error.getPosition(), os);
		// Element - Content - [1]
		os.println("<" + IError.ID_CONTENT + ">");
		os.increaseTab();
		os.println(error.getContent());
		os.decreaseTab();
		os.println("</" + IError.ID_CONTENT + ">");
		// Element - Documentation - [0..1]
		if (error.getDocumentation() != "") {
			os.println("<" + IError.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(error.getDocumentation());
			os.decreaseTab();
			os.println("</" + IError.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IError.ID_ERROR + ">");
	}

	/**
	 * 
	 * 
	 * @param object
	 * @param os
	 * @param type
	 */
	public static void writeXMLContents(IFBDObject object, XMLPrintStream os,
			String type) {
		/* Attribute Generation */
		String attribute = getFBDAttibutesString(object, type);

		/* Start Tag */
		os.println("<" + type + " " + attribute + ">");
		os.increaseTab();

		/* Elemenet Generation */
		// Elemenet - Position - [1]
		XMLWriter.writeXMLContents(object.getPosition(), os);
		// Elemenet - ConnectionPointOut - [0..1]
		if (IInVariable.ID_INVARIABLE.equals(type)
				|| IInOutVariable.ID_INOUTVARIABLE.equals(type)) {
			if (((IInVariable) object).getConnectionPointOut() != null) {
				writeXMLContents(
						((IInVariable) object).getConnectionPointOut(), os,
						IConnectionPointOut.ID_CONOUT);
			}
		}
		// Element - ConnectionPointIn - [0..1]
		if (IOutVariable.ID_OUTVARIABLE.equals(type)
				|| IInOutVariable.ID_INOUTVARIABLE.equals(type)
				|| IReturn.ID_RETURN.equals(type) || IJump.ID_JUMP.equals(type)) {
			if (((IOutVariable) object).getConnectionPointIn() != null) {
				writeXMLContents(
						((IOutVariable) object).getConnectionPointIn(), os);
			}
		}
		// Element - Expression - [1]
		if (IInVariable.ID_INVARIABLE.equals(type)
				|| IOutVariable.ID_OUTVARIABLE.equals(type)
				|| IInOutVariable.ID_INOUTVARIABLE.equals(type)) {
			os.println("<" + IInVariable.ID_EXPRESSION + ">");
			os.increaseTab();
			os.println(((IInVariable) object).getExpression());
			os.decreaseTab();
			os.println("</" + IInVariable.ID_EXPRESSION + ">");
		}
		// Element - Docuementation -[0..1]
		if (object.getDocumentation() != "") {
			os.println("<" + IFBDObject.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(object.getDocumentation());
			os.decreaseTab();
			os.println("</" + IFBDObject.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + type + ">");
	}

	public static void writeXMLContents(IFileHeader fileHeader,
			XMLPrintStream os) {
		String attribute = getAttributeString(fileHeader);
		os.println("<" + IFileHeader.ID_FILEHEADER + " " + attribute + "/>");
	}

	public static void writeXMLContents(ILDObject object, XMLPrintStream os,
			String type) {
		/* Attribute Generation */
		String attribute = getLDAttibutesString(object, type);

		/* Start Tag */
		os.println("<" + type + " " + attribute + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Position - [1]
		writeXMLContents(object.getPosition(), os);

		// Element - ConnectionPointIn
		if (ILDObject.ID_LEFTRAIL.equalsIgnoreCase(type)) {
			ILeftPowerRail lRail = (ILeftPowerRail) object;
			for (IConnectionPointOut cOut : lRail.getConnectionPointOutParams())
				writeXMLContents(cOut, os,
						IConnectionPointOut.ID_CONOUTWITHPARAM);
		}
		if (ILDObject.ID_RIGHTRAIL.equalsIgnoreCase(type)) {
			IRightPowerRail rRail = (IRightPowerRail) object;
			for (IConnectionPointIn cIn : rRail.getConnectionPointIns())
				writeXMLContents(cIn, os);
		}
		if (ILDObject.ID_COIL.equalsIgnoreCase(type)) {
			ICoil coil = (ICoil) object;
			IConnectionPointIn cIn = coil.getConnectionPointIn();
			IConnectionPointOut cOut = coil.getConnectionPointOut();
			if (cIn != null)
				writeXMLContents(cIn, os);
			if (cOut != null)
				writeXMLContents(cOut, os, IConnectionPointOut.ID_CONOUT);
			os.println("<" + ICoil.ID_VARIABLE + " >");
			os.increaseTab();
			os.println(coil.getVariable());
			os.decreaseTab();
			os.println("</" + ICoil.ID_VARIABLE + " >");
		}
		if (ILDObject.ID_CONTACT.equalsIgnoreCase(type)) {
			IContact contact = (IContact) object;
			IConnectionPointIn cIn = contact.getConnectionPointIn();
			IConnectionPointOut cOut = contact.getConnectionPointOut();
			if (cIn != null)
				writeXMLContents(cIn, os);
			if (cOut != null)
				writeXMLContents(cOut, os, IConnectionPointOut.ID_CONOUT);
			os.println("<" + IContact.ID_VARIABLE + " >");
			os.increaseTab();
			os.println(contact.getVariable());
			os.decreaseTab();
			os.println("</" + IContact.ID_VARIABLE + " >");
		}
		// Element - Documentation - [0..1]
		if (object.getDocumentation() != "") {
			os.println("<" + IError.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(object.getDocumentation());
			os.decreaseTab();
			os.println("</" + IError.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + type + ">");
	}

	public static void writeXMLContents(IPLCProject project, XMLPrintStream os) {
		// 2008.11.07
		os.println("<project xmlns=\"http://www.plcopen.org/xml/tc6.xsd\"");

		os.println("xmlns:xhtml=\"http://www.w3.org/1999/xhtml\" xmlns:xsi=");
		os
				.println("\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=");
		os
				.println("\"http://www.plcopen.org/xml/tc6.xsd http://www.plcopen.org/xml/tc6.xsd\" >");

		os.increaseTab();

		writeXMLContents(project.getFileHeader(), os);
		writeXMLContents(project.getContentHeader(), os);

		os.println("<types>");
		os.increaseTab();
		if (project.getDataTypes().isEmpty()) {
			os.println("<dataTypes />");
		} else {
			os.println("<dataTypes>");
			os.increaseTab();
			for (IDataTypeType dataType : project.getDataTypes()) {
				writeXMLContents(dataType, os);
			}
			os.decreaseTab();
			os.println("</dataTypes>");
		}
		if (project.getPOUs().isEmpty()) {
			os.println("<pous />");
		} else {
			os.println("<pous>");
			os.increaseTab();
			for (IPOU pou : project.getPOUs()) {
				writeXMLContents(pou, os);
			}
			os.decreaseTab();
			os.println("</pous>");
		}

		os.decreaseTab();
		os.println("</types>");

		// Element - instances - [1]
		os.println("<instances>");
		// sub element - configurations - [1]
		os.increaseTab();
		List<IConfiguration> cons = project.getConfigurations();
		if (cons.isEmpty()) {
			os.println("<" + IPLCProject.ID_CONFIGURATIONS + " />");
		} else {
			os.println("<" + IPLCProject.ID_CONFIGURATIONS + ">");
			// sub sub element - configuration - [0..*]
			for (IConfiguration conf : cons) {
				writeXMLContents(conf, os);
			}
			os.println("</" + IPLCProject.ID_CONFIGURATIONS + ">");
		}
		os.decreaseTab();
		os.println("</instances>");
		os.decreaseTab();
		os.println("</project>");
	}

	private static void writeXMLContents(IPLCTask task, XMLPrintStream os) {
		/* Attribute Generation */
		// Attribute - Name - Required
		StringBuffer buf = new StringBuffer();
		buf.append(IPLCTask.ID_NAME + "=\"" + task.getName() + "\" ");
		// Attribute - Single - Optional
		if (task.getSingle() != "")
			buf.append(IPLCTask.ID_SINGLE + "=\"" + task.getSingle() + "\" ");
		// Attribute - interval - Optional
		if (task.getInterval() != null)
			buf.append(IPLCTask.ID_INTERVAL + "=\""
					+ task.getInterval().toString() + "\" ");
		// Attribute - priority - Required
		buf.append(IPLCTask.ID_PRIORITY + "=\""
				+ Integer.toString(task.getPriority()) + "\" ");

		/* Start Tag */
		os.println("<" + IPLCTask.ID_TASK + " " + buf.toString() + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - pouInstance - [0..*]
		List<IPOUInstance> pous = task.getPOUInstances();
		for (IPOUInstance instance : pous) {
			writeXMLContents(instance, os);
		}
		// Element - Documentation - [0..1]
		if (task.getDocumentation() != "") {
			os.println("<" + IPLCTask.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(task.getDocumentation());
			os.decreaseTab();
			os.println("</" + IPLCTask.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IPLCTask.ID_TASK + ">");

	}

	public static void writeXMLContents(IPointerType type, XMLPrintStream os) {
		os.println("<" + IPointerType.ID_POINT + ">");
		os.increaseTab();
		os.println("<" + IPointerType.ID_BASETYPE + ">");
		os.increaseTab();
		writeXMLContents(type.getBaseType(), os);
		os.decreaseTab();
		os.println("</" + IPointerType.ID_BASETYPE + ">");
		os.decreaseTab();
		os.println("</" + IPointerType.ID_POINT + ">");
	}

	public static void writeXMLContents(IPosition position, XMLPrintStream os) {
		writeXMLContents(position, os, IPosition.ID_POSITION);
	}

	public static void writeXMLContents(IPosition position, XMLPrintStream os,
			String id) {
		os.println("<" + id + " x=\"" + position.getX() + "\" y=\""
				+ position.getY() + "\" />");
	}

	// Done
	public static void writeXMLContents(IPOU pou, XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attrubute - Name - Required
		String value = pou.getName();
		buf.append(IPOU.ID_NAME + "=\"" + value + "\" ");
		// Attribute - POUType - required
		value = pou.getPouType().toString();
		buf.append(IPOU.ID_POUTYPE + "=\"" + value + "\" ");

		/* Start Tag */
		os.println("<" + IPOU.ID_POU + " " + buf.toString() + " >");
		os.increaseTab();

		/* Element Generation */
		// Element - Interface - [0..1]
		POUInterface inf = pou.getInterface();
		if (!inf.hasNoElements())
			writeXMLContents(inf, os);
		// ELemenet - ActionType - [0..1]
		List<IActionType> actions = pou.getActionTypes();
		if (!actions.isEmpty()) {
			os.println("<" + IPOU.ID_ACTIONS + ">");
			os.increaseTab();
			for (IActionType action : actions) {
				writeXMLContents(action, os);
			}
			os.decreaseTab();
			os.println("</" + IPOU.ID_ACTIONS + ">");
		}
		// Element - TrantisionType - [0..1]
		List<ITransitionType> transitions = pou.getTransitions();
		if (!transitions.isEmpty()) {
			os.println("<" + IPOU.ID_TRANSITIONS + ">");
			os.increaseTab();
			for (ITransitionType tran : transitions) {
				writeXMLContents(tran, os);
			}
			os.decreaseTab();
			os.println("</" + IPOU.ID_TRANSITIONS + ">");
		}
		// Element - Body - [0..1]
		if (pou.getBody() != null) {
			writeXMLContents(pou.getBody(), IBody.ID_BODY, os);
		}
		// Element - Documentation - [0..1]
		if (pou.getDocumentation() != "") {
			os.println("<" + IPOU.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(pou.getDocumentation());
			os.decreaseTab();
			os.println("</" + IPOU.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IPOU.ID_POU + ">");
	}

	// DONE
	public static void writeXMLContents(IPOUInstance instance, XMLPrintStream os) {
		/* Attribute Generation */
		// Attribute - Name - Required
		StringBuffer buf = new StringBuffer();
		buf.append(IPOUInstance.ID_NAME + "=\"" + instance.getName() + "\" ");
		// Attribute - Type - Required
		buf.append(IPOUInstance.ID_TYPE + "=\"" + instance.getType().toString()
				+ "\" ");

		/* Start Tag */
		os.println("<" + IPOUInstance.ID_POUINSTANCE + " " + buf.toString()
				+ ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Documentation - [0..1]
		if (instance.getDocumentation() != "") {
			os.println("<" + IPOUInstance.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(instance.getDocumentation());
			os.decreaseTab();
			os.println("</" + IPOUInstance.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IPOUInstance.ID_POUINSTANCE + ">");
	}

	private static void writeXMLContents(IRange range, String ID,
			XMLPrintStream os) {
		os.println("<" + ID + " " + IRange.ID_LOWER + "=\"" + range.getLower()
				+ "\"" + " " + IRange.ID_UPPER + "=\"" + range.getUpper()
				+ "\" />");
	}

	private static void writeXMLContents(IResource resource, XMLPrintStream os) {
		/* Attribute Generation */
		// Attribute - Name - Required
		StringBuffer buf = new StringBuffer();
		buf.append(IResource.ID_NAME + "=\"" + resource.getName() + "\" ");

		/* Start Tag */
		os.println("<" + IResource.ID_RESOURCE + " " + buf.toString() + ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Task [0..*]
		List<IPLCTask> tasks = resource.getTasks();
		for (IPLCTask task : tasks) {
			writeXMLContents(task, os);
		}
		// Element - globalVars [0..*]
		List<IVariableList> vars = resource.getGlobalVars();
		for (IVariableList var : vars) {
			writeXMLContents(var, IResource.ID_GLOBALVARS, false, os);
		}
		// Element - pouInstances [0..*]
		List<IPOUInstance> instances = resource.getPouInstance();
		for (IPOUInstance instance : instances) {
			writeXMLContents(instance, os);
		}
		// Element - Documentation - [0..1]
		if (resource.getDocumentation() != "") {
			os.println("<" + IResource.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(resource.getDocumentation());
			os.decreaseTab();
			os.println("</" + IResource.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IResource.ID_RESOURCE + ">");
	}

	public static void writeXMLContents(ISFCObject obj, XMLPrintStream os,
			String type) {
		String attribute = getSFCAttibutesString(obj, type);

		/* Start Tag */
		os.println("<" + type + " " + attribute + ">");
		os.increaseTab();

		writeXMLContents(obj.getPosition(), os);

		IConnectionPointIn in = null;
		if (ISFCObject.ID_STEP.equals(type))
			in = ((IStep) obj).getConnectionPointIn();
		else if (ISFCObject.ID_TRANSITION.equals(type))
			in = ((ITransition) obj).getConnectionPointIn();
		else if (ISFCObject.ID_MARCOSTEP.equals(type))
			in = ((IMacroStep) obj).getConnectionPointIn();
		else if (ISFCObject.ID_JUMPSTEP.equals(type))
			in = ((IJumpStep) obj).getConnectionPointIn();
		else if (ISFCObject.ID_SELDIV.equals(type))
			in = ((ISelectiveDivergence) obj).getConnectionPointIn();
		else if (ISFCObject.ID_SIMULDIV.equals(type))
			in = ((ISimultaneousDivergence) obj).getConnectionPointIn();

		if (in != null) {
			writeXMLContents(in, os);
		}

		List<IConnectionPointIn> ins = null;
		if (ISFCObject.ID_SELCON.equals(type))
			ins = ((ISelectiveConvergence) obj).getConnectionPointIns();
		else if (ISFCObject.ID_SIMULCON.equals(type))
			ins = ((ISimultaneousConvergence) obj).getConnectionPointIns();

		if (ins != null && !ins.isEmpty()) {
			for (IConnectionPointIn pointin : ins) {
				writeXMLContents(pointin, os);
			}
		}

		IConnectionPointOut out = null;
		if (ISFCObject.ID_TRANSITION.equals(type))
			out = ((ITransition) obj).getConnectionPointOut();
		else if (ISFCObject.ID_SIMULCON.equals(type))
			out = ((ISimultaneousConvergence) obj).getConnectionPointOut();
		else if (ISFCObject.ID_SELCON.equals(type))
			out = ((ISelectiveConvergence) obj).getConnectionPointOut();
		else if (ISFCObject.ID_MARCOSTEP.equals(type))
			out = ((IMacroStep) obj).getConnectionPointOut();

		if (out != null) {
			writeXMLContents(out, os, IConnectionPointOut.ID_CONOUT);
		}

		if (ISFCObject.ID_STEP.equals(type)) {
			IConnectionPointOut pointout = ((IStep) obj)
					.getConnectionPointOut();
			if (pointout != null) {
				writeXMLContents(pointout, os,
						IConnectionPointOut.ID_CONOUTWITHPARAM);
			}
		}

		List<IConnectionPointOut> outs = null;
		if (ISFCObject.ID_SELDIV.equals(type))
			outs = ((ISelectiveDivergence) obj).getConnectionPointOuts();
		else if (ISFCObject.ID_SIMULDIV.equals(type))
			outs = ((ISimultaneousDivergence) obj).getConnectionPointOuts();

		if (outs != null && !outs.isEmpty()) {
			for (IConnectionPointOut pointout : outs) {
				writeXMLContents(pointout, os,
						IConnectionPointOut.ID_CONOUTWITHPARAM);
			}
		}

		if (ISFCObject.ID_STEP.equals(type)) {
			IConnectionPointOut action = ((IStep) obj)
					.getConnectionPointOutAction();
			if (action != null) {
				writeXMLContents(action, os,
						IConnectionPointOut.ID_CONOUTACTION);
			}
		}

		if (ISFCObject.ID_TRANSITION.equals(type)) {
			ICondition condition = ((ITransition) obj).getCondition();
			if (condition != null) {
				writeXMLContents(condition, os);
			}
		}

		if (obj.getDocumentation() != "") {
			os.println("<" + ISFCObject.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(obj.getDocumentation());
			os.decreaseTab();
			os.println("</" + ISFCObject.ID_DOCUMENTATION + ">");
		}

		os.decreaseTab();
		os.println("</" + type + ">");
	}

	public static void writeXMLContents(ITransitionType tran, XMLPrintStream os) {
		/* Attribute Generation */
		// Attribute - Name - Required
		StringBuffer buf = new StringBuffer();
		buf.append(ITransitionType.ID_NAME + "=\"" + tran.getName() + "\" ");

		/* Start Tag */
		os.println("<" + ITransitionType.ID_TRANSITION + " " + buf.toString()
				+ ">");
		os.increaseTab();

		/* Element Generation */
		// Element - Body - [1]
		writeXMLContents(tran.getBody(), IBody.ID_BODY, os);
		// Element - Documentation - [0..1]
		if (tran.getDocumentation() != "") {
			os.println("<" + ITransitionType.ID_DOCUMENTATION + ">");
			os.increaseTab();
			os.println(tran.getDocumentation());
			os.decreaseTab();
			os.println("</" + ITransitionType.ID_DOCUMENTATION + ">");
		}

		/* End Tag */
		os.decreaseTab();
		os.println("</" + IActionType.ID_ACTION + ">");

	}

	// DONE
	public static void writeXMLContents(IValue value, XMLPrintStream os) {
		if (value instanceof ISimpleValue) {
			ISimpleValue sValue = (ISimpleValue) value;
			if (sValue != null)
				os.println("<" + IValue.ID_SIMPLEVALUE + " " + IValue.ID_VALUE
						+ "=\"" + sValue.getValue() + "\" />");
			else
				os.println("<" + IValue.ID_SIMPLEVALUE + " />");
		} else if (value instanceof IArrayValue) {
			IArrayValue aValue = (IArrayValue) value;
			os.println("<" + IValue.ID_ARRAYVALUE + " >");
			os.increaseTab();
			for (IArrayValueType vType : aValue.getValues()) {
				writeValueSubXMLContents(vType, os);
			}
			os.decreaseTab();
			os.println("</" + IValue.ID_ARRAYVALUE + " >");
		} else if (value instanceof IStructValue) {
			IStructValue sValue = (IStructValue) value;
			os.println("<" + IValue.ID_STRUCTVALUE + " >");
			os.increaseTab();
			for (IStructValueType vType : sValue.getValues()) {
				writeValueSubXMLContents(vType, os);
			}
			os.decreaseTab();
			os.println("</" + IValue.ID_STRUCTVALUE + " >");
		}

	}

	// DONE
	public static void writeXMLContents(IVariable pVar, XMLPrintStream os) {
		/* Attribute Generation */
		StringBuffer buf = new StringBuffer();
		// Attribute - Name - required
		buf.append(IVariable.ID_NAME + "=\"" + pVar.getName() + "\" ");
		// Attribute - Address - optional
		if (pVar.getAddress() != null && pVar.getAddress() != "")
			buf
					.append(IVariable.ID_ADDRESS + "=\"" + pVar.getAddress()
							+ "\" ");

		/* Start Tag */
		os.print
