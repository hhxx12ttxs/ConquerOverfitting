package plcopen.xml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import plcopen.exceptions.InvalidDocumentException;
import plcopen.exceptions.InvalidTypeException;
import plcopen.inf.model.IActionType;
import plcopen.inf.model.IConfiguration;
import plcopen.inf.model.IContentHeader;
import plcopen.inf.model.ICoordinationInfo;
import plcopen.inf.model.IDataTypeType;
import plcopen.inf.model.IFileHeader;
import plcopen.inf.model.IPLCElement;
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
import plcopen.inf.type.IStructValueType;
import plcopen.inf.type.IValue;
import plcopen.inf.type.IVariableList;
import plcopen.inf.type.POUType;
import plcopen.inf.type.StorageModifierType;
import plcopen.inf.type.Time;
import plcopen.inf.type.body.IFBD;
import plcopen.inf.type.group.common.IAction;
import plcopen.inf.type.group.common.IActionBlock;
import plcopen.inf.type.group.common.IComment;
import plcopen.inf.type.group.common.ICommonObject;
import plcopen.inf.type.group.common.IConnector;
import plcopen.inf.type.group.common.IContinuation;
import plcopen.inf.type.group.common.IError;
import plcopen.inf.type.group.common.Qual;
import plcopen.inf.type.group.derived.IArray;
import plcopen.inf.type.group.derived.IDerived;
import plcopen.inf.type.group.derived.IDerivedType;
import plcopen.inf.type.group.derived.IEnum;
import plcopen.inf.type.group.derived.IRangeType;
import plcopen.inf.type.group.derived.IStruct;
import plcopen.inf.type.group.elementary.IElementaryType;
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
import plcopen.model.ConfigurationImpl;
import plcopen.model.ContentHeaderImpl;
import plcopen.model.CoordinationInfoImpl;
import plcopen.model.DataTypeType;
import plcopen.model.FileHeaderImpl;
import plcopen.model.POU;
import plcopen.model.POUBehaviorImpl;
import plcopen.model.POUInterface;
import plcopen.model.ProjectImpl;
import plcopen.model.ResourceImpl;
import plcopen.model.TaskImpl;
import plcopen.model.VariableImpl;
import plcopen.type.ArrayValue;
import plcopen.type.Connection;
import plcopen.type.ConnectionPointInImpl;
import plcopen.type.ConnectionPointOutImpl;
import plcopen.type.POUInstance;
import plcopen.type.Position;
import plcopen.type.Range;
import plcopen.type.RangeType;
import plcopen.type.SimpleValue;
import plcopen.type.StructValue;
import plcopen.type.VarList;
import plcopen.type.body.FBD;
import plcopen.type.body.IL;
import plcopen.type.body.LD;
import plcopen.type.body.SFC;
import plcopen.type.body.ST;
import plcopen.type.group.datatype.ArrayType;
import plcopen.type.group.datatype.DerivedType;
import plcopen.type.group.datatype.ElementaryType;
import plcopen.type.group.datatype.EnumType;
import plcopen.type.group.datatype.PointerType;
import plcopen.type.group.poubody.ActionImpl;
import plcopen.type.group.poubody.BlockImpl;
import plcopen.type.group.poubody.CommonImpl;
import plcopen.type.group.poubody.ConditionImpl;
import plcopen.type.group.poubody.FBDObjectImpl;
import plcopen.type.group.poubody.LDObjectImpl;
import plcopen.type.group.poubody.SFCObjectImpl;
import plcopen.type.group.poubody.VariableImplInBlock;

/**
 * 
 * 
 * @author swkim
 */
public class PLCModel {
	/**
	 * 
	 * 
	 * @param actNode
	 * @return IAction
	 * @throws InvalidDocumentException
	 */
	private static IAction getAction(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		// 
		IAction action = new ActionImpl(parent);

		/* Attribute */
		NamedNodeMap maps = node.getAttributes();
		// Qualifier - attribute - optional
		Node attribute = maps.getNamedItem(IAction.ID_QUALIFIER);
		String name = (attribute == null) ? "" : attribute.getNodeValue();
		action.setQualifier(new Qual(name));
		// duration -attribute - optional
		attribute = maps.getNamedItem(IAction.ID_DURATION);
		String duration = (attribute == null) ? "" : attribute.getNodeValue();
		if (duration != "")
			action.setDuration(duration);
		// indicator - attribute - optional
		attribute = maps.getNamedItem(IAction.ID_INDICATOR);
		String indicator = (attribute == null) ? "" : attribute.getNodeValue();
		if (indicator != "")
			action.setIndicator(indicator);

		/* Element */
		NodeList childList = node.getChildNodes();
		// Reference - Element [0..1]
		Node refNode = PLCModel.getNodeByName(childList, IAction.ID_REFERENCE);
		if (refNode != null) {
			//
			IReference ref = new ConditionImpl();
			/* Attribute */
			NamedNodeMap cMaps = node.getAttributes();
			Node refNameNode = cMaps.getNamedItem(IReference.ID_NAME);
			String refName = (refNameNode == null) ? "" : refNameNode
					.getNodeValue();
			if (refName != "") {
				ref.setName(refName);
				action.setReference(ref);
			}
		}
		// Inline - Element [0..1]
		Node inlineNode = PLCModel.getNodeByName(childList, IAction.ID_INLINE);
		if (inlineNode != null) {

			IBody body = PLCModel.getBody(inlineNode, action);
			action.setInline(body);
		}
		// Documentation - Element [0..1]
		Node docuNode = PLCModel.getNodeByName(childList,
				IAction.ID_DOCUMENTATION);
		if (docuNode != null) {
			String documentation = PLCModel.getString(docuNode);
			action.setDocumentation(documentation);
		}
		return action;
	}

	/**
	 * 
	 * 
	 * @param node
	 * @param parent
	 * @return IActionType
	 * @throws InvalidDocumentException
	 */
	private static IActionType getActionType(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		// 
		POUBehaviorImpl type = new POUBehaviorImpl();
		type.setParent(parent);

		/* Attribute */
		NamedNodeMap maps = node.getAttributes();
		// Name - Attribute - required
		Node attribute = maps.getNamedItem(IActionType.ID_NAME);
		String name = (attribute == null) ? "" : attribute.getNodeValue();
		if (name == "")
			throw new InvalidDocumentException("IActionType",
					"name attribute is missing");
		type.setName(name);

		/* Element */
		NodeList childList = node.getChildNodes();
		// Body - Element [1]
		Node bodyNode = PLCModel.getNodeByName(childList, IBody.ID_BODY);
		if (bodyNode == null)
			throw new InvalidDocumentException("ActionType", "Body is missing");
		IBody body = PLCModel.getBody(bodyNode, type);
		type.setBody(body);
		// Documentation - Element [0..1]
		Node docuNode = PLCModel.getNodeByName(childList,
				IActionType.ID_DOCUMENTATION);
		if (docuNode != null) {
			String documentation = PLCModel.getString(docuNode);
			type.setDocumentation(documentation);
		}
		return type;
	}

	/**
	 * DerivedType
	 * 
	 * @param node
	 * @param parent
	 * @return
	 * @throws InvalidDocumentException
	 */
	private static IArray getArrayType(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		// IArrayType
		ArrayType type = new ArrayType();
		type.setParent(parent);

		/* Element */
		NodeList childList = node.getChildNodes();
		// rangeSigned - element [1..*]
		List<Node> dimNodes = PLCModel.getNodeListByName(childList,
				IArray.ID_DIMENSION);
		if (dimNodes.size() < 1)
			throw new InvalidDocumentException("IArrayType",
					IArray.ID_DIMENSION);
		List<IRange> dims = new ArrayList<IRange>();
		for (Node dimNode : dimNodes) {
			IRange dim = PLCModel.getRange(dimNode, true, type);
			dims.add(dim);
		}
		type.setDimensions(dims);

		// BaseType - Element [1]
		Node baseNode = PLCModel.getNodeByName(childList, IArray.ID_BASETYPE);
		if (baseNode == null)
			throw new InvalidDocumentException("IArrayType", IArray.ID_BASETYPE);
		//TODO: dslab
		//PLCModel myModel = new PLCModel();
		IDataType bType = PLCModel.getDataType(baseNode, type);
		type.setBaseType(bType);

		return type;
	}

	/**
	 * 
	 * 
	 * 
	 * @param node
	 * @param ID
	 * @return
	 */
	private static String getAttributebyName(Node node, String ID) {
		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(ID);
		String name = (attribute == null) ? "" : attribute.getNodeValue();
		return name;
	}

	private static IBlock getBlock(Node node) throws InvalidDocumentException {
		BlockImpl block = new BlockImpl();

		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(IBlock.ID_LOCALID);
		String localID = (attribute == null) ? "" : attribute.getNodeValue();
		if (localID == "")
			throw new InvalidDocumentException("Block", IBlock.ID_LOCALID
					+ "is missing");
		block.setLocalID(Long.parseLong(localID));

		attribute = maps.getNamedItem(IBlock.ID_WIDTH);
		String width = (attribute == null) ? "" : attribute.getNodeValue();
		if (width != "")
			block.setSizeWidth((int) Long.parseLong(width));

		attribute = maps.getNamedItem(IBlock.ID_HEIGHT);
		String height = (attribute == null) ? "" : attribute.getNodeValue();
		if (height != "")
			block.setSizeHeight((int) Long.parseLong(height));

		attribute = maps.getNamedItem(IBlock.ID_TYPENAME);
		String typeName = (attribute == null) ? "" : attribute.getNodeValue();
		if (typeName == "")
			throw new InvalidDocumentException("Block", IBlock.ID_TYPENAME
					+ "is missing");
		block.setTypeName(typeName);

		attribute = maps.getNamedItem(IBlock.ID_INSTANCENAME);
		String instanceName = (attribute == null) ? "" : attribute
				.getNodeValue();
		if (instanceName != "")
			block.setInstanceName(instanceName);

		attribute = maps.getNamedItem(IBlock.ID_EXECUTIONID);
		String orderID = (attribute == null) ? "" : attribute.getNodeValue();
		if (orderID != "")
			block.setExecutionOrderID(Integer.parseInt(orderID));

		NodeList childList = node.getChildNodes();

		Node posNode = PLCModel.getNodeByName(childList, IBlock.ID_POSITION);
		IPosition pos = getPosition(posNode);
		block.setPosition(pos);

		Node subNode = PLCModel.getNodeByName(childList, IBlock.ID_INVARIABLES);
		if (subNode == null)
			throw new InvalidDocumentException("Block", IBlock.ID_INVARIABLES
					+ "element is missing");
		List<IInVariableInBlock> list = new ArrayList<IInVariableInBlock>();
		NodeList superChildList = subNode.getChildNodes();
		List<Node> varList = PLCModel.getNodeListByName(superChildList,
				IVariableInBlock.ID_VARIABLE);
		for (Node superChildNode : varList) {
			list.add((IInVariableInBlock) PLCModel.getVariableInBlock(
					superChildNode, IBlock.ID_INVARIABLES));
		}
		block.setInVariables(list);

		subNode = PLCModel.getNodeByName(childList, IBlock.ID_OUTVARIABLES);
		if (subNode == null)
			throw new InvalidDocumentException("Block", IBlock.ID_OUTVARIABLES
					+ "element is missing");
		List<IOutVariableInBlock> outList = new ArrayList<IOutVariableInBlock>();
		superChildList = subNode.getChildNodes();
		varList = PLCModel.getNodeListByName(superChildList,
				IVariableInBlock.ID_VARIABLE);
		for (Node superChildNode : varList) {
			outList.add((IOutVariableInBlock) PLCModel.getVariableInBlock(
					superChildNode, IBlock.ID_OUTVARIABLES));
		}
		block.setOutVariables(outList);

		subNode = PLCModel.getNodeByName(childList, IBlock.ID_INOUTVARIABLES);
		if (subNode == null)
			throw new InvalidDocumentException("Block",
					IBlock.ID_INOUTVARIABLES + "element is missing");
		List<IInOutVariableInBlock> inoutList = new ArrayList<IInOutVariableInBlock>();
		superChildList = subNode.getChildNodes();
		varList = PLCModel.getNodeListByName(superChildList,
				IVariableInBlock.ID_VARIABLE);
		for (Node superChildNode : varList) {
			inoutList.add((IInOutVariableInBlock) PLCModel.getVariableInBlock(
					superChildNode, IBlock.ID_INVARIABLES));
		}
		block.setInOutVariables(inoutList);

		subNode = PLCModel.getNodeByName(childList, IBlock.ID_ADDDATA);
		if(subNode != null){
			//subNode -> addData
			NodeList addDataChildList = subNode.getChildNodes();
			subNode = PLCModel.getNodeByName(addDataChildList, "data");
			//subNode -> data
			maps = subNode.getAttributes();
			attribute = maps.getNamedItem(IBlock.ID_ADDDATA_NAME);
			String addDataName = (attribute == null) ? "" : attribute.getNodeValue();
			if(addDataName == "")
				throw new InvalidDocumentException("Block", IBlock.ID_ADDDATA_NAME + "is missing");
			block.setAddDataName(addDataName);
			
			attribute = maps.getNamedItem(IBlock.ID_ADDDATA_HANDLEUNKNOWN);
			String addDataHandleUnknown = (attribute == null) ? "" : attribute.getNodeValue();
			if(addDataName == "")
				throw new InvalidDocumentException("Block", IBlock.ID_ADDDATA_HANDLEUNKNOWN + "is missing");
			block.setAddDataHandleUnknown(addDataHandleUnknown);
		}
			
		
		Node docuNode = PLCModel.getNodeByName(childList,
				IBlock.ID_DOCUMENTATION);
		if (docuNode != null) {
			String str = PLCModel.getString(docuNode);
			block.setDocumentation(str);
		}
		return block;
	}

	/**
	 * 
	 * 
	 * @param node
	 *            the node object of a XML element corresponding to the node
	 *            "Body"
	 * @param parent
	 *            parent object
	 * @return
	 * @throws InvalidDocumentException
	 */
	private static IBody getBody(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		NodeList childList = node.getChildNodes();

		IBody body = null;

		Node ilNode = getNodeByName(childList, IBody.ID_IL);
		if (ilNode != null) {
			String str = getString(ilNode);
			IL il = new IL();
			il.setContent(str);
			if (body != null)
				throw new InvalidDocumentException("body",
						"Too many body element");
			body = il;
		}

		Node stNode = getNodeByName(childList, IBody.ID_ST);
		if (stNode != null) {
			String str = getString(stNode);
			ST st = new ST();
			st.setContent(str);
			if (body != null)
				throw new InvalidDocumentException("body",
						"Too many body element");
			body = st;
		}

		Node fbdNode = getNodeByName(childList, IBody.ID_FBD);
		if (fbdNode != null) {
			IFBD fbd = PLCModel.getFBD(fbdNode, parent);
			if (body != null)
				throw new InvalidDocumentException("body",
						"Too many body element");
			body = fbd;
		}

		Node ldNode = getNodeByName(childList, IBody.ID_LD);
		if (ldNode != null) {
			LD ld = PLCModel.getLD(ldNode, parent);
			if (body != null)
				throw new InvalidDocumentException("body",
						"Too many body element");
			body = ld;
		}

		Node sfcNode = getNodeByName(childList, IBody.ID_SFC);
		if (sfcNode != null) {
			SFC sfc = PLCModel.getSFC(sfcNode, parent);
			if (body != null)
				throw new InvalidDocumentException("body",
						"Too many body element");
			body = sfc;
		}

		Node docuNode = getNodeByName(childList, IBody.ID_DOCUMENTATION);
		if (docuNode != null) {
			String str = getString(docuNode);
			body.setDocumentation(str);
		}
		return body;
	}

	private static ConnectionPointOutImpl getCommonConnectionPointOut(
			ConnectionPointOutImpl cout, Node node)
			throws InvalidDocumentException {
		NodeList childList = node.getChildNodes();

		Node posNode = PLCModel.getNodeByName(childList,
				IConnectionPointOut.ID_RELPOSITION);
		if (posNode != null) {
			IPosition pos = getPosition(posNode,
					IConnectionPointOut.ID_RELPOSITION);
			cout.setRelativePosition(pos);
		}

		Node expNode = PLCModel.getNodeByName(childList,
				IConnectionPointOut.ID_EXPRESSION);
		if (expNode != null) {
			String expression = PLCModel.getExpressionString(expNode);
			cout.setExpression(expression);
		}

		return cout;
	}

	private static ICommonObject getCommonObject(Node node, String type)
			throws InvalidDocumentException {
		// 
		CommonImpl common = new CommonImpl();

		// Attribute
		NamedNodeMap maps = node.getAttributes();
		// Attribute
		if (ICommonObject.ID_ACTIONBLK.equals(type)) {
			// Action Block, Negated, Attribute
			Node attribute = maps.getNamedItem(IActionBlock.ID_NEGATED);
			String negatedStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			boolean negated = false;
			if (negatedStr != "")
				negated = Boolean.parseBoolean(negatedStr);
			common.setNegated(negated);
		} else {
			// Attribute
			Node attribute = maps.getNamedItem(ICommonObject.ID_LOCALID);
			String localID = attribute.getNodeValue();
			attribute = maps.getNamedItem(ICommonObject.ID_WIDTH);
			String width = (attribute == null) ? "" : attribute.getNodeValue();
			attribute = maps.getNamedItem(ICommonObject.ID_HEIGHT);
			String height = (attribute == null) ? "" : attribute.getNodeValue();
			attribute = maps.getNamedItem(IConnector.ID_NAME);
			String name = (attribute == null) ? "" : attribute.getNodeValue();

			// Attribute
			if (localID == null || localID == "")
				throw new InvalidDocumentException("", ICommonObject.ID_LOCALID);
			common.setLocalID(Long.parseLong(localID));

			if (ICommonObject.ID_COMMENT.equals(type)
					|| ICommonObject.ID_ERROR.equals(type)) {
				if (width == "" || height == "")
					throw new InvalidDocumentException("",
							ICommonObject.ID_WIDTH + ":"
									+ ICommonObject.ID_HEIGHT);
			} else {
				if (name == "")
					throw new InvalidDocumentException("", IConnector.ID_NAME);
			}
			if (width != "")
				common.setSizeWidth((int) Long.parseLong(width));
			if (height != "")
				common.setSizeHeight((int) Long.parseLong(height));
			if (name != "")
				common.setName(name);
		}

		// Element
		NodeList childList = node.getChildNodes();

		// Position
		Node posNode = PLCModel.getNodeByName(childList,
				ICommonObject.ID_POSITION);
		if (posNode != null) {
			Position pos = getPosition(posNode);
			common.setPosition(pos);
		}
		// Documentation
		Node docuNode = PLCModel.getNodeByName(childList,
				ICommonObject.ID_DOCUMENTATION);
		if (docuNode != null) {
			String documentation = PLCModel.getString(docuNode);
			common.setDocumentation(documentation);
		}
		// Content
		if (ICommonObject.ID_COMMENT.equals(type)
				|| ICommonObject.ID_ERROR.equals(type)) {
			Node commentNode = PLCModel.getNodeByName(childList,
					IComment.ID_CONTENT);
			if (commentNode != null) {
				String content = PLCModel.getString(commentNode);
				common.setContent(content);
			}
		}
		// ConnectionPointIn
		if (ICommonObject.ID_CONNECTOR.equals(type)
				|| ICommonObject.ID_ACTIONBLK.equals(type)) {
			Node inNode = PLCModel.getNodeByName(childList,
					IConnectionPointIn.ID_CONIN);
			if (inNode != null) {
				IConnectionPointIn cin = PLCModel.getConnectionPointIn(inNode);
				common.setConnectionPointIn(cin);
			}
		}
		// ConnectionPointOut
		if (ICommonObject.ID_CONTINUATION.equals(type)
				|| ICommonObject.ID_ACTIONBLK.equals(type)) {
			Node outNode = PLCModel.getNodeByName(childList,
					IConnectionPointOut.ID_CONOUT);
			if (outNode != null) {
				IConnectionPointOut cout = PLCModel
						.getConnectionPointOut(outNode);
				common.setConnectionPointOut(cout);
			}
		}
		// Action
		if (ICommonObject.ID_ACTIONBLK.equals(type)) {
			List<Node> actNodes = PLCModel.getNodeListByName(childList,
					IActionBlock.ID_ACTION);
			List<IAction> acts = new ArrayList<IAction>();
			for (Node actNode : actNodes) {
				IAction act = PLCModel.getAction(actNode, common);
				acts.add(act);
			}
			common.setActions(acts);
		}

		return common;
	}

	private static VariableImplInBlock getCommonVariableInBlock(Node node)
			throws InvalidDocumentException {
		VariableImplInBlock var = new VariableImplInBlock();

		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(IVariableInBlock.ID_FORMALPARAM);
		String param = (attribute == null) ? "" : attribute.getNodeValue();
		if (param == "")
			throw new InvalidDocumentException("Variable",
					IVariableInBlock.ID_FORMALPARAM + "is missing");
		var.setFormalParameter(param);

		attribute = maps.getNamedItem(IVariableInBlock.ID_NEGATED);
		String negatedStr = (attribute == null) ? "" : attribute.getNodeValue();
		boolean negated = false;
		if (negatedStr != "")
			negated = Boolean.parseBoolean(negatedStr);
		var.setNegated(negated);

		attribute = maps.getNamedItem(IVariableInBlock.ID_HIDDEN);
		String hiddenStr = (attribute == null) ? "" : attribute.getNodeValue();
		boolean hidden = false;
		if (hiddenStr != "")
			hidden = Boolean.parseBoolean(hiddenStr);
		var.setHidden(hidden);

		attribute = maps.getNamedItem(IVariableInBlock.ID_EDGE);
		String edgeStr = (attribute == null) ? "" : attribute.getNodeValue();
		EdgeModifierType edge = EdgeModifierType.getInstance(edgeStr);
		var.setEdge(edge);

		attribute = maps.getNamedItem(IVariableInBlock.ID_STORAGE);
		String storageStr = (attribute == null) ? "" : attribute.getNodeValue();
		StorageModifierType storage = StorageModifierType
				.getInstance(storageStr);
		var.setStorage(storage);

		return var;
	}

	/**
	 * 
	 * 
	 * @param node
	 * @return
	 * @throws InvalidDocumentException
	 */
	private static ICondition getCondition(Node node)
			throws InvalidDocumentException {
		ConditionImpl impl = new ConditionImpl();
		NodeList childList = node.getChildNodes();

		// Connection
		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(ISFCObject.ID_NEGATED);
		String negatedStr = (attribute == null) ? "" : attribute.getNodeValue();
		boolean negated = false;
		if (negatedStr != "")
			negated = Boolean.parseBoolean(negatedStr);
		impl.setNegated(negated);

		// 

		// 1. Reference element
		Node refNode = PLCModel.getNodeByName(childList,
				ICondition.ID_REFERENCE);
		if (refNode != null) {
			String reference = PLCModel.getAttributebyName(refNode,
					ICondition.ID_NAME);
			if (reference == "")
				throw new InvalidDocumentException("Condition",
						ICondition.ID_NAME + "(reference) is missing");
			impl.setName(reference);
			impl.setType(ICondition.ID_REFERENCE);
		}

		// 2.
		List<Node> conNodes = PLCModel.getNodeListByName(childList,
				Connection.ID_CONNECTION);
		if (!conNodes.isEmpty()) {
			List<IConnection> connections = new ArrayList<IConnection>();
			for (Node conNode : conNodes) {
				Connection con = PLCModel.getConnection(conNode);
				connections.add(con);
			}

			// 
			if (impl.getType() != null)
				throw new InvalidDocumentException("Condition",
						"condition has more element");

			impl.setConnections(connections);
			impl.setType(ICondition.ID_CONLIST);
		}

		// 3.
		Node inlineNode = PLCModel.getNodeByName(childList,
				ICondition.ID_INLINE);
		if (inlineNode != null) {
			IBody body = PLCModel.getBody(inlineNode, null);

			// 
			if (impl.getType() != null)
				throw new InvalidDocumentException("Condition",
						"condition has more element");

			String name = PLCModel.getAttributebyName(inlineNode,
					ICondition.ID_NAME);
			if (name != null && name != "") {
				impl.setName(name);
				impl.setBody(body);
				impl.setType(ICondition.ID_INLINE);
			}

			else
				throw new InvalidDocumentException("inline", ICondition.ID_NAME
						+ " attribute problem");
		}

		// 
		if (impl.getType() == null)
			throw new InvalidDocumentException("Condition",
					"no appropriated element");
		return impl;
	}

	/**
	 * XML
	 * 
	 * @param node
	 * @param project
	 * @return
	 * @throws InvalidDocumentException
	 */
	private static IConfiguration getConfiguration(Node node,
			IPLCElement project) throws InvalidDocumentException {
		// IConfiguration
		ConfigurationImpl conf = new ConfigurationImpl();
		conf.setParent(project);

		/* Attribute */
		NamedNodeMap maps = node.getAttributes();
		// Name - Attribute - required
		Node attribute = maps.getNamedItem(IConfiguration.ID_NAME);
		String name = (attribute == null) ? "" : attribute.getNodeValue();
		if (name == "")
			throw new InvalidDocumentException("Configuration",
					IConfiguration.ID_NAME);
		conf.setName(name);

		/* Element */
		NodeList childList = node.getChildNodes();
		// Resource - Element [0..*]
		List<Node> resNodes = getNodeListByName(childList,
				IConfiguration.ID_RESOURCE);
		List<IResource> ress = new ArrayList<IResource>();
		for (Node resNode : resNodes) {
			IResource res = getResource(resNode, project);
			ress.add(res);
		}
		conf.setResources(ress);
		// Global Variables - Element [0..*]
		List<Node> gVarNodes = getNodeListByName(childList,
				IConfiguration.ID_GLOBALVARS);
		List<IVariableList> gvars = new ArrayList<IVariableList>();
		for (Node gVarNode : gVarNodes) {
			IVariableList gvar = getVarList(gVarNode, true, project);
			gvars.add(gvar);
		}
		conf.setGlobalVars(gvars);
		// Documentation - Element, [0..1]
		Node docuNode = getNodeByName(childList,
				IConfiguration.ID_DOCUMENTATION);
		if (docuNode != null) {
			String str = getString(docuNode);
			conf.setDocumentation(str);
		}

		return conf;
	}

	/**
	 * XML
	 * 
	 * @param node
	 * @return
	 * @throws InvalidDocumentException
	 */
	private static Connection getConnection(Node node)
			throws InvalidDocumentException {
		Connection con = new Connection();

		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(Connection.ID_REFLOCALID);
		String id = (attribute == null) ? "" : attribute.getNodeValue();
		if (id == "")
			throw new InvalidDocumentException("Connection",
					Connection.ID_REFLOCALID + "is missing");
		con.setRefLocalID(Integer.parseInt(id));

		attribute = maps.getNamedItem(Connection.ID_FORMALPARAM);
		String param = (attribute == null) ? "" : attribute.getNodeValue();
		if (param != "")
			con.setFormalParam(param);

		NodeList childList = node.getChildNodes();
		List<Node> posNodes = getNodeListByName(childList,
				Connection.ID_POSITION);
		List<IPosition> positions = new ArrayList<IPosition>();
		for (Node posNode : posNodes) {
			IPosition pos = getPosition(posNode, Connection.ID_POSITION);
			positions.add(pos);
		}
		con.setPositions(positions);

		return con;
	}

	private static IConnectionPointIn getConnectionPointIn(Node node)
			throws InvalidDocumentException {
		ConnectionPointInImpl in = new ConnectionPointInImpl();

		NodeList childList = node.getChildNodes();
		Node posNode = PLCModel.getNodeByName(childList,
				IConnectionPointIn.ID_RELPOSITION);
		if (posNode != null) {
			IPosition pos = getPosition(posNode,
					IConnectionPointIn.ID_RELPOSITION);
			in.setRelativePosition(pos);
		}

		List<IConnection> cons = new ArrayList<IConnection>();
		Node expNode = PLCModel.getNodeByName(childList,
				IConnectionPointIn.ID_EXPRESSION);
		List<Node> conNodes = PLCModel.getNodeListByName(childList,
				IConnectionPointIn.ID_CONNETION);
		if ((expNode != null) && (conNodes.size() > 0))
			throw new InvalidDocumentException("Variable",
					IInVariableInBlock.ID_CONIN + "element is missing");
		if (expNode != null) {
			String expression = PLCModel.getExpressionString(expNode);
			in.setExpression(expression);
		} else if (conNodes.size() > 0) {
			for (Node conNode : conNodes) {
				Connection con = PLCModel.getConnection(conNode);
				cons.add(con);
			}
			in.setConnections(cons);
		}

		return in;
	}

	private static IConnectionPointOut getConnectionPointOut(Node node)
			throws InvalidDocumentException {
		ConnectionPointOutImpl cout = new ConnectionPointOutImpl();

		return getCommonConnectionPointOut(cout, node);
	}

	private static IConnectionPointOut getConnectionPointOutWithFormalParam(
			Node node) throws InvalidDocumentException {
		ConnectionPointOutImpl impl = new ConnectionPointOutImpl();

		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(IConnectionPointOut.ID_FORMALPARAM);
		String param = attribute.getNodeValue();
		if (param == null || param == "")
			throw new InvalidDocumentException("SFC", ISFCObject.ID_LOCALID);
		impl.setFormalParameter(param);

		return getCommonConnectionPointOut(impl, node);
	}

	/**
	 * XML
	 * 
	 * @param node
	 * @param parent
	 * @return
	 * @throws InvalidDocumentException
	 */
	private static IContentHeader getContentHeader(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		// 
		ContentHeaderImpl contentHeader = new ContentHeaderImpl();
		contentHeader.setParent(parent);

		/* Attribute */
		NamedNodeMap maps = node.getAttributes();
		// Name - Attribute - required
		Node attribute = maps.getNamedItem(IContentHeader.ID_NAME);
		String name = (attribute == null) ? "" : attribute.getNodeValue();
		if (name == "")
			throw new InvalidDocumentException("ContentHeader",
					IContentHeader.ID_NAME);
		contentHeader.setName(name);
		// Version - attribute
		attribute = maps.getNamedItem(IContentHeader.ID_VERSION);
		String version = (attribute == null) ? "" : attribute.getNodeValue();
		contentHeader.setVersion(version);

		attribute = maps.getNamedItem(IContentHeader.ID_DATE);
		String date = (attribute == null) ? "" : attribute.getNodeValue();
		if (date != null && date != "")
			contentHeader.setDate(DateTime.parseString(date));

		attribute = maps.getNamedItem(IContentHeader.ID_ORGANIZATION);
		String organization = (attribute == null) ? "" : attribute
				.getNodeValue();
		contentHeader.setOrganization(organization);

		attribute = maps.getNamedItem(IContentHeader.ID_AUTHOR);
		String author = (attribute == null) ? "" : attribute.getNodeValue();
		contentHeader.setAuthor(author);

		attribute = maps.getNamedItem(IContentHeader.ID_LANGUAGE);
		String language = (attribute == null) ? "" : attribute.getNodeValue();
		contentHeader.setLanguage(language);

		NodeList childList = node.getChildNodes();

		Node commentNode = PLCModel.getNodeByName(childList,
				IContentHeader.ID_COMMENT);
		if (commentNode != null) {
			String str = PLCModel.getExpressionString(commentNode);
			contentHeader.setComment(str);
		}

		Node coordNode = PLCModel.getNodeByName(childList,
				IContentHeader.ID_COORINFO);
		if (coordNode == null)
			throw new InvalidDocumentException("ContentHeader",
					IContentHeader.ID_COORINFO);
		ICoordinationInfo info = PLCModel.getCoordinationInfo(coordNode,
				contentHeader);
		contentHeader.setCoordinationInfo(info);

		return contentHeader;
	}

	/**
	 * XML
	 * 
	 * @param coordNode
	 * @param parent
	 * @return
	 * @throws InvalidDocumentException
	 */
	private static ICoordinationInfo getCoordinationInfo(Node coordNode,
			IPLCElement parent) throws InvalidDocumentException {
		ICoordinationInfo info = new CoordinationInfoImpl();
		info.setParent(parent);

		NodeList childList = coordNode.getChildNodes();

		Node pageSizeNode = PLCModel.getNodeByName(childList, "pageSize");
		if (pageSizeNode != null) {
			Position pageSize = new Position(0, 0);
			NamedNodeMap maps = pageSizeNode.getAttributes();

			Node attribute = maps.getNamedItem("x");
			String xStr = (attribute == null) ? "" : attribute.getNodeValue();
			if (xStr == null || xStr == "")
				throw new InvalidDocumentException("pageSize", "x");

			attribute = maps.getNamedItem("y");
			String yStr = (attribute == null) ? "" : attribute.getNodeValue();
			if (yStr == null || yStr == "")
				throw new InvalidDocumentException("pageSize", "y");

			pageSize.setX(Integer.parseInt(xStr));
			pageSize.setY(Integer.parseInt(yStr));

			info.setPageSize(pageSize);
		}

		Node fbdScalingNode = PLCModel.getNodeByName(childList, "fbd");
		if (fbdScalingNode == null)
			throw new InvalidDocumentException(IContentHeader.ID_COORINFO,
					"fbd");
		Node subNode = PLCModel.getNodeByName(fbdScalingNode.getChildNodes(),
				ICoordinationInfo.ID_SCALING);
		Position pos = getPosition(subNode, ICoordinationInfo.ID_SCALING);
		if (pos == null)
			throw new InvalidDocumentException(IContentHeader.ID_COORINFO,
					"fbd scaling");
		info.setFBDScaling(pos);

		Node ldScalingNode = PLCModel.getNodeByName(childList, "ld");
		if (ldScalingNode == null)
			throw new InvalidDocumentException(IContentHeader.ID_COORINFO, "ld");
		subNode = PLCModel.getNodeByName(ldScalingNode.getChildNodes(),
				ICoordinationInfo.ID_SCALING);
		pos = getPosition(subNode, ICoordinationInfo.ID_SCALING);
		if (pos == null)
			throw new InvalidDocumentException(IContentHeader.ID_COORINFO,
					"ld scaling");
		info.setLDScaling(pos);

		Node sfcScalingNode = PLCModel.getNodeByName(childList, "sfc");
		if (sfcScalingNode == null)
			throw new InvalidDocumentException(IContentHeader.ID_COORINFO,
					"sfc");
		subNode = PLCModel.getNodeByName(sfcScalingNode.getChildNodes(),
				ICoordinationInfo.ID_SCALING);
		pos = getPosition(subNode, ICoordinationInfo.ID_SCALING);
		if (pos == null)
			throw new InvalidDocumentException(IContentHeader.ID_COORINFO,
					"sfc scaling");
		info.setSFCScaling(pos);

		return info;
	}

	private static IDataType getDataType(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		IDataType type = null;
		NodeList childList = node.getChildNodes();
		for (int i = 0; i < childList.getLength(); i++) {
			Node cNode = childList.item(i);

			try {
				IElementaryType eType = PLCModel.getElementaryTypes(cNode);
				if (type != null)
					throw new InvalidDocumentException("DataType",
							"Too many type element");
				type = eType;
			} catch (InvalidTypeException e) {
			}

			try {
				IDerivedType dType = PLCModel.getDerivedTypes(cNode, parent);
				if (type != null)
					throw new InvalidDocumentException("DataType",
							"Too many type element");
				type = dType;
			} catch (InvalidTypeException e) {
			}

			try {
				IExtendedType exType = PLCModel.getExtendedTypes(cNode, parent);
				if (type != null)
					throw new InvalidDocumentException("DataType",
							"Too many type element");
				type = exType;
			} catch (InvalidTypeException e) {
			}
		}

		return type;
	}

	private static IDataTypeType getDataTypeType(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		DataTypeType type = new DataTypeType();
		type.setParent(parent);

		/* Attribute */
		NamedNodeMap maps = node.getAttributes();
		// Name - Attribute - required
		Node attribute = maps.getNamedItem(IDataTypeType.ID_NAME);
		String name = (attribute == null) ? "" : attribute.getNodeValue();
		if (name == "")
			throw new InvalidDocumentException("IDataTypeType",
					IDataTypeType.ID_NAME);
		type.setName(name);

		/* Element */
		NodeList childList = node.getChildNodes();
		// BaseType - Element [1]
		Node baseTypeNode = getNodeByName(childList, IDataTypeType.ID_BASETYPE);
		if (baseTypeNode == null)
			throw new InvalidDocumentException("IDataTypeType",
					IDataTypeType.ID_BASETYPE);
		//TODO: dslab
		//PLCModel myModel = new PLCModel();
		type.setBaseType(PLCModel.getDataType(baseTypeNode, type));
		// InitialValue - Element [0..1]
		Node initialValueNode = getNodeByName(childList,
				IDataTypeType.ID_INITIALVALUE);
		if (initialValueNode != null) {
			try {
				IValue value = getValue(initialValueNode, type);
				type.setInitialValue(value);
			} catch (InvalidTypeException e) {
				throw new InvalidDocumentException("IDataTypeType",
						IDataTypeType.ID_BASETYPE);
			}
		}
		// Documentation - Element, [0..1]
		Node docuNode = getNodeByName(childList, IDataTypeType.ID_DOCUMENTATION);
		if (docuNode != null) {
			String str = getString(docuNode);
			type.setDocumentation(str);
		}
		return type;
	}

	/**
	 * DerivedType
	 * 
	 * @param node
	 * @param parent
	 * @return
	 * @throws NoGivenTypeException
	 * @throws InvalidDocumentException
	 */
	private static IDerivedType getDerivedTypes(Node node, IPLCElement parent)
			throws InvalidTypeException, InvalidDocumentException {
		IDerivedType type = null;
		
		/* Elemenet */
		NodeList childList = node.getChildNodes();
		// ArrayType
		Node arrayNode = getNodeByName(childList, IDerivedType.ID_ARRAY);
		if (arrayNode != null) {
			IArray aType = PLCModel.getArrayType(arrayNode, parent);
			if (type != null)
				throw new InvalidTypeException("Array");
			type = aType;
		}
		// DerivedType ver.DSlab
		Node derivedNode = null;
		if (node.getNodeName().equals(IDerivedType.ID_DERIVED)) {
			derivedNode = node;
			IDerived aType = new DerivedType();
			NamedNodeMap maps = derivedNode.getAttributes();
			Node attribute = maps.getNamedItem(IDerived.ID_NAME);
			String name = (attribute == null) ? "" : attribute.getNodeValue();
			if (name == "")
				throw new InvalidDocumentException("IDerivedType",
						IDerived.ID_NAME);
			//TODO: The name of derived type should be exist in the interface list.
			aType.setName(name);
			type = aType;
		}
		// DerivedType
		/*
		Node derivedNode = getNodeByName(childList, IDerivedType.ID_DERIVED);
		if (derivedNode != null) {
			IDerived aType = new DerivedType();
			NamedNodeMap maps = derivedNode.getAttributes();
			Node attribute = maps.getNamedItem(IDerived.ID_NAME);
			String name = (attribute == null) ? "" : attribute.getNodeValue();
			if (name == "")
				throw new InvalidDocumentException("IDerivedType",
						IDerived.ID_NAME);
			type = aType;
		}*/
		// EnumType
		Node enumNode = getNodeByName(childList, IDerivedType.ID_ENUM);
		if (enumNode != null) {
			IEnum aType = PLCModel.getEnumType(enumNode, parent);
			if (type != null)
				throw new InvalidTypeException("Enum");
			type = aType;
		}
		// StructType
		Node structNode = getNodeByName(childList, IDerivedType.ID_STRUCT);
		if (structNode != null) {
			IStruct aType = (IStruct) PLCModel.getVarList(structNode, true,
					parent);
			if (type != null)
				throw new InvalidTypeException("Struct");
			type = aType;
		}
		// SubRangeSignedType
		Node subRangeSignedNode = getNodeByName(childList,
				IDerivedType.ID_SUBRANGESIGNED);
		if (subRangeSignedNode != null) {
			IRangeType aType = PLCModel.getSubRangeType(subRangeSignedNode,
					true, parent);
			if (type != null)
				throw new InvalidTypeException("subrangeSigned");
			type = aType;
		}
		
		//SubRangeUnsignedType ver.Dslab
		Node subRangeUnsignedNode = null;
		if(node.getNodeName().equals(IDerivedType.ID_SUBRANGEUNSIGNED))
			subRangeUnsignedNode = node;
		if (subRangeUnsignedNode != null) {
			IRangeType aType = PLCModel.getSubRangeType(subRangeUnsignedNode, false, parent);
			if (type != null)
				throw new InvalidTypeException("subrangeUnsigned");
			type = aType;
		}
		/*
		// SubRangeUnsignedType
		Node subRangeUnsignedNode = getNodeByName(childList,
				IDerivedType.ID_SUBRANGEUNSIGNED);
		if (subRangeUnsignedNode != null) {
			IRangeType aType = PLCModel.getSubRangeType(subRangeUnsignedNode,
					false, parent);
			if (type != null)
				throw new InvalidTypeException("subrangeUnsigned");
			type = aType;
		}
		//*/ 
		if (type == null)
			throw new InvalidTypeException("DerivedValue");

		return type;
	}

	private static IElementaryType getElementaryTypes(Node node)
			throws InvalidDocumentException, InvalidTypeException {

		IElementaryType type = null;

		String name = node.getNodeName();
		if (IElementaryType.ID_BOOL.equals(name))
			type = new ElementaryType(IElementaryType.ID_BOOL);
		else if (IElementaryType.ID_BYTE.equals(name))
			type = new ElementaryType(IElementaryType.ID_BYTE);
		else if (IElementaryType.ID_DATE.equals(name))
			type = new ElementaryType(IElementaryType.ID_DATE);
		else if (IElementaryType.ID_DINT.equals(name))
			type = new ElementaryType(IElementaryType.ID_DINT);
		else if (IElementaryType.ID_DWORD.equals(name))
			type = new ElementaryType(IElementaryType.ID_DWORD);
		else if (IElementaryType.ID_INT.equals(name))
			type = new ElementaryType(IElementaryType.ID_INT);
		else if (IElementaryType.ID_LINT.equals(name))
			type = new ElementaryType(IElementaryType.ID_LINT);
		else if (IElementaryType.ID_LREAL.equals(name))
			type = new ElementaryType(IElementaryType.ID_LREAL);
		else if (IElementaryType.ID_LWORD.equals(name))
			type = new ElementaryType(IElementaryType.ID_LWORD);
		else if (IElementaryType.ID_REAL.equals(name))
			type = new ElementaryType(IElementaryType.ID_REAL);
		else if (IElementaryType.ID_SINT.equals(name))
			type = new ElementaryType(IElementaryType.ID_SINT);
		else if (IElementaryType.ID_DT.equals(name))
			type = new ElementaryType(IElementaryType.ID_DT);
		else if (IElementaryType.ID_TIME.equals(name))
			type = new ElementaryType(IElementaryType.ID_TIME);
		else if (IElementaryType.ID_TOD.equals(name))
			type = new ElementaryType(IElementaryType.ID_TOD);
		else if (IElementaryType.ID_UDINT.equals(name))
			type = new ElementaryType(IElementaryType.ID_UDINT);
		else if (IElementaryType.ID_UINT.equals(name))
			type = new ElementaryType(IElementaryType.ID_UINT);
		else if (IElementaryType.ID_ULINT.equals(name))
			type = new ElementaryType(IElementaryType.ID_ULINT);
		else if (IElementaryType.ID_USINT.equals(name))
			type = new ElementaryType(IElementaryType.ID_USINT);
		else if (IElementaryType.ID_WORD.equals(name))
			type = new ElementaryType(IElementaryType.ID_WORD);
		else if (IElementaryType.ID_STRING.equals(name)) {
			ElementaryType eType = ElementaryType.createStringType();
			NamedNodeMap maps = node.getAttributes();
			Node attribute = maps.getNamedItem(IElementaryType.ID_LENGTH);
			String lengthStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			long length = Long.parseLong(lengthStr);
			length = (length < 0) ? 0 : length;
			if (name != "")
				eType.setLength(length);
			type = eType;
		} else if (IElementaryType.ID_WSTRING.equals(name)) {
			ElementaryType eType = ElementaryType.createWStringType();
			NamedNodeMap maps = node.getAttributes();
			Node attribute = maps.getNamedItem(IElementaryType.ID_LENGTH);
			String lengthStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			long length = Long.parseLong(lengthStr);
			length = (length < 0) ? 0 : length;
			if (name != "")
				eType.setLength(length);
			type = eType;
		}
		if (type == null)
			throw new InvalidTypeException("ElementaryType");

		return type;
	}

	private static IEnum getEnumType(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		// 
		EnumType type = new EnumType();
		type.setParent(parent);

		/* Element */
		NodeList childList = node.getChildNodes();
		// values - Element [1]
		Node vNode = PLCModel.getNodeByName(childList, IEnum.ID_VALUES);
		if (vNode != null)
			throw new InvalidDocumentException("EnumType", IEnum.ID_VALUES);
		// 
		List<Node> nodes = PLCModel.getNodeListByName(vNode.getChildNodes(),
				IEnum.ID_VALUE);
		List<IEnum.Pair> values = new ArrayList<IEnum.Pair>();
		for (Node valueNode : nodes) {
			IEnum.Pair p = PLCModel.getPairValue(valueNode, parent);
			values.add(p);
		}
		type.setValues(values);
		// BaseType - Element [0..1]
		Node baseNode = PLCModel.getNodeByName(childList, IEnum.ID_BASETYPE);
		if (baseNode != null) {
			//TODO: dslab
			//PLCModel myModel = new PLCModel();
			IDataType bType = PLCModel.getDataType(baseNode, type);
			type.setBaseType(bType);
		}
		return type;
	}

	private static String getExpressionString(Node node) {
		String str = node.getChildNodes().item(0).getNodeValue();
		return str.trim();
	}

	// not implemented
	private static IExtendedType getExtendedTypes(Node node, IPLCElement parent)
			throws InvalidTypeException, InvalidDocumentException {
		PointerType type = new PointerType();
		type.setParent(parent);

		NodeList childList = node.getChildNodes();
		Node pNode = getNodeByName(childList, IExtendedType.ID_POINT);
		if (pNode == null)
			throw new InvalidTypeException("ExtendedType");

		NodeList superChildList = pNode.getChildNodes();
		Node bNode = getNodeByName(superChildList, PointerType.ID_BASETYPE);
		if (bNode == null)
			throw new InvalidDocumentException("Pointer", "baseType is missing");
		//TODO: dslab
		//PLCModel myModel = new PLCModel();
		type.setBaseType(PLCModel.getDataType(bNode, type));

		return type;
	}

	private static IFBD getFBD(Node fbdNode, IPLCElement parent)
			throws InvalidDocumentException {
		IFBD fbd = new FBD(parent);
		PLCModel.getFBDInstance(fbd, fbdNode);
		return fbd;
	}

	private static FBDObjectImpl getFBDCommonImpl(Node node)
			throws InvalidDocumentException {
		FBDObjectImpl impl = new FBDObjectImpl();

		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(IFBDObject.ID_LOCALID);
		String localID = attribute.getNodeValue();
		if (localID == null || localID == "")
			throw new InvalidDocumentException("RETURN", IFBDObject.ID_LOCALID);
		impl.setLocalID(Long.parseLong(localID));

		attribute = maps.getNamedItem(IBlock.ID_WIDTH);
		String width = (attribute == null) ? "" : attribute.getNodeValue();
		if (width != "")
			impl.setSizeWidth((int) Long.parseLong(width));

		attribute = maps.getNamedItem(IBlock.ID_HEIGHT);
		String height = (attribute == null) ? "" : attribute.getNodeValue();
		if (height != "")
			impl.setSizeHeight((int) Long.parseLong(height));

		attribute = maps.getNamedItem(IBlock.ID_EXECUTIONID);
		String orderID = (attribute == null) ? "" : attribute.getNodeValue();
		if (orderID != "")
			impl.setExecutionOrderID(Integer.parseInt(orderID));

		NodeList childList = node.getChildNodes();
		Node posNode = getNodeByName(childList, IFBDObject.ID_POSITION);
		if (posNode != null) {
			Position pos = getPosition(posNode);
			impl.setPosition(pos);
		}

		Node docuNode = getNodeByName(childList, IFBDObject.ID_DOCUMENTATION);
		if (docuNode != null) {
			String documentation = getString(docuNode);
			impl.setDocumentation(documentation);
		}

		return impl;
	}

	/**
	 * 
	 * @param fbd
	 * @param fbdNode
	 * @throws InvalidDocumentException
	 */
	private static void getFBDInstance(IFBD fbd, Node fbdNode)
			throws InvalidDocumentException {
		NodeList childList = fbdNode.getChildNodes();

		// Comment
		List<Node> list = getNodeListByName(childList, ICommonObject.ID_COMMENT);
		List<IComment> comments = new ArrayList<IComment>();
		for (Node subNode : list) {
			IComment comment = (IComment) getCommonObject(subNode,
					ICommonObject.ID_COMMENT);
			comments.add(comment);
		}
		fbd.setComments(comments);

		// Error
		list = getNodeListByName(childList, ICommonObject.ID_ERROR);
		List<IError> errs = new ArrayList<IError>();
		for (Node subNode : list) {
			IError err = (IError) getCommonObject(subNode,
					ICommonObject.ID_ERROR);
			errs.add(err);
		}
		fbd.setErrors(errs);

		// connector
		list = getNodeListByName(childList, ICommonObject.ID_CONNECTOR);
		List<IConnector> connectors = new ArrayList<IConnector>();
		for (Node subNode : list) {
			IConnector connector = (IConnector) getCommonObject(subNode,
					ICommonObject.ID_CONNECTOR);
			connectors.add(connector);
		}
		fbd.setConnectors(connectors);

		// continuation
		list = getNodeListByName(childList, ICommonObject.ID_CONTINUATION);
		List<IContinuation> continuations = new ArrayList<IContinuation>();
		for (Node subNode : list) {
			IContinuation continuation = (IContinuation) getCommonObject(
					subNode, ICommonObject.ID_CONTINUATION);
			continuations.add(continuation);
		}
		fbd.setContinuations(continuations);

		// Action Block
		list = getNodeListByName(childList, ICommonObject.ID_ACTIONBLK);
		List<IActionBlock> aBlocks = new ArrayList<IActionBlock>();
		for (Node subNode : list) {
			IActionBlock aBlock = (IActionBlock) getCommonObject(subNode,
					ICommonObject.ID_ACTIONBLK);
			aBlocks.add(aBlock);
		}
		fbd.setActionBlks(aBlocks);

		// Block
		list = getNodeListByName(childList, IFBD.ID_BLOCK);
		List<IBlock> blocks = new ArrayList<IBlock>();
		for (Node subNode : list) {
			IBlock block = PLCModel.getBlock(subNode);
			blocks.add(block);
		}
		fbd.setBlocks(blocks);

		// Input Variable
		list = getNodeListByName(childList, IInVariable.ID_INVARIABLE);
		List<IInVariable> inVars = new ArrayList<IInVariable>();
		for (Node subNode : list) {
			IInVariable var = PLCModel.getFBDObject(subNode,
					IInVariable.ID_INVARIABLE);
			inVars.add(var);
		}
		fbd.setInVariables(inVars);

		list = getNodeListByName(childList, IOutVariable.ID_OUTVARIABLE);
		List<IOutVariable> outVars = new ArrayList<IOutVariable>();
		for (Node subNode : list) {
			IOutVariable var = PLCModel.getFBDObject(subNode,
					IOutVariable.ID_OUTVARIABLE);
			outVars.add(var);
		}
		fbd.setOutVariables(outVars);

		list = getNodeListByName(childList, IInOutVariable.ID_INOUTVARIABLE);
		List<IInOutVariable> inoutVars = new ArrayList<IInOutVariable>();
		for (Node subNode : list) {
			IInOutVariable var = PLCModel.getFBDObject(subNode,
					IInOutVariable.ID_INOUTVARIABLE);
			inoutVars.add(var);
		}
		fbd.setInOutVariables(inoutVars);

		list = getNodeListByName(childList, ILabel.ID_LABEL);
		List<ILabel> labels = new ArrayList<ILabel>();
		for (Node subNode : list) {
			ILabel var = PLCModel.getFBDObject(subNode, ILabel.ID_LABEL);
			labels.add(var);
		}
		fbd.setLabels(labels);

		list = getNodeListByName(childList, IJump.ID_JUMP);
		List<IJump> jumps = new ArrayList<IJump>();
		for (Node subNode : list) {
			IJump var = PLCModel.getFBDObject(subNode, IJump.ID_JUMP);
			jumps.add(var);
		}
		fbd.setJumps(jumps);

		list = getNodeListByName(childList, IReturn.ID_RETURN);
		List<IReturn> rets = new ArrayList<IReturn>();
		for (Node subNode : list) {
			IReturn var = PLCModel.getFBDObject(subNode, IReturn.ID_RETURN);
			rets.add(var);
		}
		fbd.setReturns(rets);
	}

	private static FBDObjectImpl getFBDObject(Node node, String id)
			throws InvalidDocumentException {
		FBDObjectImpl impl = PLCModel.getFBDCommonImpl(node);
		NamedNodeMap maps = node.getAttributes();
		NodeList childList = node.getChildNodes();

		// Expression
		if (IInVariable.ID_INVARIABLE.equals(id)
				|| IOutVariable.ID_OUTVARIABLE.equals(id)
				|| IInOutVariable.ID_INOUTVARIABLE.equals(id)) {
			Node expNode = getNodeByName(childList, IInVariable.ID_EXPRESSION);
			if (expNode != null) {
				String exp = getExpressionString(expNode);
				impl.setExpression(exp);
			}
		}

		// negated, edge, storage
		if (IInVariable.ID_INVARIABLE.equals(id)
				|| IOutVariable.ID_OUTVARIABLE.equals(id)) {
			Node attribute = maps.getNamedItem(IFBDObject.ID_NEGATED);
			String negatedStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			boolean negated = false;
			if (negatedStr != "")
				negated = Boolean.parseBoolean(negatedStr);
			impl.setNegated(negated);

			attribute = maps.getNamedItem(IInVariable.ID_EDGE);
			String edgeStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			EdgeModifierType edge = EdgeModifierType.getInstance(edgeStr);
			impl.setEdge(edge);

			attribute = maps.getNamedItem(IInVariable.ID_STORAGE);
			String storageStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			StorageModifierType storage = StorageModifierType
					.getInstance(storageStr);
			impl.setStorage(storage);
		}

		if (IInVariable.ID_INVARIABLE.equals(id)
				|| IInOutVariable.ID_INOUTVARIABLE.equals(id)) {
			Node coutNode = getNodeByName(childList,
					IConnectionPointOut.ID_CONOUT);
			if (coutNode != null) {
				IConnectionPointOut cout = PLCModel
						.getConnectionPointOut(coutNode);
				impl.setConnectionPointOut(cout);
			}

		}
		if (IOutVariable.ID_OUTVARIABLE.equals(id)
				|| IInOutVariable.ID_INOUTVARIABLE.equals(id)
				|| IJump.ID_JUMP.equals(id) || IReturn.ID_RETURN.equals(id)) {
			Node cinNode = getNodeByName(childList, IConnectionPointIn.ID_CONIN);
			if (cinNode != null) {
				IConnectionPointIn cin = PLCModel.getConnectionPointIn(cinNode);
				impl.setConnectionPointIn(cin);
			}
		}
		if (IInOutVariable.ID_INOUTVARIABLE.equals(id)) {

			Node attribute = maps.getNamedItem(IInOutVariable.ID_NEGATEDIN);
			String negatedStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			boolean negated = false;
			if (negatedStr != "")
				negated = Boolean.parseBoolean(negatedStr);
			impl.setNegatedIn(negated);

			attribute = maps.getNamedItem(IInOutVariable.ID_NEGATEDOUT);
			negatedStr = (attribute == null) ? "" : attribute.getNodeValue();
			negated = false;
			if (negatedStr != "")
				negated = Boolean.parseBoolean(negatedStr);
			impl.setNegatedOut(negated);

			attribute = maps.getNamedItem(IInOutVariable.ID_EDGEIN);
			String edgeStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			EdgeModifierType edge = EdgeModifierType.getInstance(edgeStr);
			impl.setEdgeIn(edge);

			attribute = maps.getNamedItem(IInOutVariable.ID_EDGEOUT);
			edgeStr = (attribute == null) ? "" : attribute.getNodeValue();
			edge = EdgeModifierType.getInstance(edgeStr);
			impl.setEdgeOut(edge);

			attribute = maps.getNamedItem(IInOutVariable.ID_STORAGEIN);
			String storageStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			StorageModifierType storage = StorageModifierType
					.getInstance(storageStr);
			impl.setStorageIn(storage);

			attribute = maps.getNamedItem(IInOutVariable.ID_STORAGEOUT);
			storageStr = (attribute == null) ? "" : attribute.getNodeValue();
			storage = StorageModifierType.getInstance(storageStr);
			impl.setStorageOut(storage);

		}

		if (IJump.ID_JUMP.equals(id)) {
			Node attribute = maps.getNamedItem(IJump.ID_LABEL);
			String label = attribute.getNodeValue();
			if (label == null || label == "")
				throw new InvalidDocumentException(IJump.ID_LABEL,
						IJump.ID_LABEL);
			impl.setLabel(label);

		}
		if (ILabel.ID_LABEL.equals(id)) {
			Node attribute = maps.getNamedItem(ILabel.ID_LABEL);
			String label = attribute.getNodeValue();
			if (label == null || label == "")
				throw new InvalidDocumentException(ILabel.ID_LABEL,
						ILabel.ID_LABEL);
			impl.setLabel(label);

		}
		return impl;
	}

	/**
	 * 
	 */
	private static IFileHeader getFileHeader(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		IFileHeader fileHeader = new FileHeaderImpl();
		fileHeader.setParent(parent);

		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(IFileHeader.ID_COM_NAME);
		String companyName = attribute.getNodeValue();
		if (companyName == null || companyName == "")
			throw new InvalidDocumentException("FileHeader",
					IFileHeader.ID_COM_NAME);
		fileHeader.setCompanyName(companyName);

		attribute = maps.getNamedItem(IFileHeader.ID_COM_URL);
		String companyURL = (attribute == null) ? "" : attribute.getNodeValue();
		fileHeader.setCompanyURL(companyURL);

		attribute = maps.getNamedItem(IFileHeader.ID_PRO_NAME);
		String productName = (attribute == null) ? "" : attribute
				.getNodeValue();
		if (productName == null || productName == "")
			throw new InvalidDocumentException("FileHeader",
					IFileHeader.ID_PRO_NAME);
		fileHeader.setProductName(productName);

		attribute = maps.getNamedItem(IFileHeader.ID_PRO_VER);
		String productVersion = (attribute == null) ? "" : attribute
				.getNodeValue();
		if (productVersion == null || productVersion == "")
			throw new InvalidDocumentException("FileHeader",
					IFileHeader.ID_PRO_VER);
		fileHeader.setProductVersion(productVersion);

		attribute = maps.getNamedItem(IFileHeader.ID_PRO_REL);
		String productRelease = (attribute == null) ? "" : attribute
				.getNodeValue();
		if (productRelease == null)
			productRelease = "";
		fileHeader.setProductRelease(productRelease);

		attribute = maps.getNamedItem(IFileHeader.ID_DATE);
		String creationDateTimeStr = (attribute == null) ? "" : attribute
				.getNodeValue();
		if (creationDateTimeStr != "") {
			fileHeader.setCreationDateTime(DateTime
					.parseString(creationDateTimeStr));
		}

		attribute = maps.getNamedItem(IFileHeader.ID_DESCRIPTION);
		String contentDescription = (attribute == null) ? "" : attribute
				.getNodeValue();
		if (contentDescription == null)
			contentDescription = "";
		fileHeader.setContentDescription(contentDescription);

		return fileHeader;
	}

	private static VarList getInitVarList(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		//
		VarList varList = new VarList();
		varList.setPlain(true);

		/* Element */
		NodeList childList = node.getChildNodes();
		// Variable - Element, [0..*]
		List<Node> vNodes = PLCModel.getNodeListByName(childList,
				IVariableList.ID_VARIABLE);
		List<IVariable> vList = new ArrayList<IVariable>();
		for (Node vNode : vNodes) {
			vList.add(PLCModel.getVariable(vNode, parent));
		}
		varList.setVariables(vList);
		// Documentation - Element, [0..1]
		Node docuNode = PLCModel.getNodeByName(childList,
				IVariableList.ID_DOCUMENTATION);
		if (docuNode != null) {
			String value = PLCModel.getString(docuNode);
			varList.setDocumentation(value);
		}

		return varList;
	}

	private static LD getLD(Node node, IPLCElement parent)
			throws InvalidDocumentException {
		LD ld = new LD(parent);

		PLCModel.getLDInstance(ld, node);

		return ld;
	}

	private static LDObjectImpl getLDCommonImpl(Node node)
			throws InvalidDocumentException {
		LDObjectImpl impl = new LDObjectImpl();

		NamedNodeMap maps = node.getAttributes();
		Node attribute = maps.getNamedItem(ILDObject.ID_LOCALID);
		String localID = attribute.getNodeValue();
		if (localID == null || localID == "")
			throw new InvalidDocumentException("LD", ILDObject.ID_LOCALID);
		impl.setLocalID(Long.parseLong(localID));

		attribute = maps.getNamedItem(ILDObject.ID_WIDTH);
		String width = (attribute == null) ? "" : attribute.getNodeValue();
		if (width != "")
			impl.setSizeWidth((int) Long.parseLong(width));

		attribute = maps.getNamedItem(ILDObject.ID_HEIGHT);
		String height = (attribute == null) ? "" : attribute.getNodeValue();
		if (height != "")
			impl.setSizeHeight((int) Long.parseLong(height));

		NodeList childList = node.getChildNodes();
		Node posNode = getNodeByName(childList, ILDObject.ID_POSITION);
		if (posNode != null) {
			IPosition pos = getPosition(posNode);
			impl.setPosition(pos);
		}

		Node docuNode = getNodeByName(childList, ILDObject.ID_DOCUMENTATION);
		if (docuNode != null) {
			String documentation = getString(docuNode);
			impl.setDocumentation(documentation);
		}

		return impl;
	}

	private static void getLDInstance(LD ld, Node node)
			throws InvalidDocumentException {

		getFBDInstance(ld, node);

		NodeList childList = node.getChildNodes();

		List<Node> list = getNodeListByName(childList, ILDObject.ID_LEFTRAIL);
		List<ILeftPowerRail> rails = new ArrayList<ILeftPowerRail>();
		for (Node subNode : list) {
			ILeftPowerRail block = PLCModel.getLDObject(subNode,
					ILDObject.ID_LEFTRAIL);
			rails.add(block);
		}
		ld.setLeftPowerRail(rails);

		list = getNodeListByName(childList, ILDObject.ID_RIGHTRAIL);
		List<IRightPowerRail> rightRails = new ArrayList<IRightPowerRail>();
		for (Node subNode : list) {
			IRightPowerRail rail = PLCModel.getLDObject(subNode,
					ILDObject.ID_RIGHTRAIL);
			rightRails.add(rail);
		}
		ld.setRightPowerRail(rightRails);

		list = getNodeListByName(childList, ILDObject.ID_COIL);
		List<ICoil> coils = new ArrayList<ICoil>();
		for (Node subNode : list) {
			ICoil coil = PLCModel.getLDObject(subNode, ILDObject.ID_COIL);
			coils.add(coil);
		}
		ld.setCoils(coils);

		list = getNodeListByName(childList, ILDObject.ID_CONTACT);
		List<IContact> contacts = new ArrayList<IContact>();
		for (Node subNode : list) {
			IContact contact = PLCModel.getLDObject(subNode,
					ILDObject.ID_CONTACT);
			contacts.add(contact);
		}
		ld.setContacts(contacts);
	}

	private static LDObjectImpl getLDObject(Node node, String id)
			throws InvalidDocumentException {
		LDObjectImpl impl = PLCModel.getLDCommonImpl(node);
		NamedNodeMap maps = node.getAttributes();
		NodeList childList = node.getChildNodes();

		if (ILDObject.ID_COIL.equals(id) || ILDObject.ID_CONTACT.equals(id)) {
			Node attribute = maps.getNamedItem(ILDObject.ID_EXECUTIONID);
			String orderID = (attribute == null) ? "" : attribute
					.getNodeValue();
			if (orderID != "")
				impl.setExecutionOrderID(Long.parseLong(orderID));

			attribute = maps.getNamedItem(ILDObject.ID_NEGATED);
			String negatedStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			boolean negated = false;
			if (negatedStr != "")
				negated = Boolean.parseBoolean(negatedStr);
			impl.setNegated(negated);

			attribute = maps.getNamedItem(ILDObject.ID_EDGE);
			String edgeStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			EdgeModifierType edge = EdgeModifierType.getInstance(edgeStr);
			impl.setEdge(edge);

			attribute = maps.getNamedItem(ILDObject.ID_STORAGE);
			String storageStr = (attribute == null) ? "" : attribute
					.getNodeValue();
			StorageModifierType storage = StorageModifierType
					.getInstance(storageStr);
			impl.setStorage(storage);
		}

		if (ILDObject.ID_LEFTRAIL.equals(id)) {
			List<Node> outNodes = getNodeListByName(childList,
					IConnectionPointOut.ID_CONOUT);
			List<IConnectionPointOut> list = new ArrayList<IConnectionPointOut>();
			for (Node cinNode : outNodes) {
				IConnectionPointOut cin = PLCModel
						.getConnectionPointOut(cinNode);
				list.add(cin);
			}
			impl.setConnectionPointOutParams(list);
		}

		if (ILDObject.ID_RIGHTRAIL.equals(id)) {
			List<Node> inNodes = getNodeListByName(childList,
					IConnectionPointIn.ID_CONIN);
			List<IConnectionPointIn> list = new ArrayList<IConnectionPointIn>();
			for (Node cinNode : inNodes) {
				IConnectionPointIn cin = PLCModel.getConnectionPointIn(cinNode);
				list.add(cin);
			}
			impl.setConnectionPointIns(list);
		}
		if (ILDObject.ID_COIL.equals(id) || ILDObject.ID_CONTACT.equals(id)) {
			Node cinNode = getNodeByName(childList, IConnectionPointIn.ID_CONIN);
			if (cinNode != null) {
				IConnectionPointIn cin = PLCModel.getConnectionPointIn(cinNode);
				impl.setConnectionPointIn(cin);
			}

			Node coutNode = getNodeByName(childList,
					IConnectionPointOut.ID_CONOUT);
			if (coutNode != null) {
				IConnectionPointOut cout = PLCModel
						.getConnectionPointOut(coutNode);
				impl.setConnectionPointOut(cout);
			}

			Node varNode = getNodeByName(childList, ICoil.ID_VARIABLE);
			if (varNode == null)
				throw new InvalidDocumentException("LD", ILDObject.ID_VARIABLE);

			String str = getExpressionString(varNode);
			if (str == "")
				throw new InvalidDocumentExce
