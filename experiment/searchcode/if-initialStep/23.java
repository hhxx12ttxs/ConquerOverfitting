package plcopen.type.group.poubody;

import java.util.ArrayList;
import java.util.List;

import plcopen.inf.type.IBody;
import plcopen.inf.type.IConnectionPointIn;
import plcopen.inf.type.IConnectionPointOut;
import plcopen.inf.type.group.sfc.ICondition;
import plcopen.inf.type.group.sfc.IJumpStep;
import plcopen.inf.type.group.sfc.IMacroStep;
import plcopen.inf.type.group.sfc.ISelectiveConvergence;
import plcopen.inf.type.group.sfc.ISelectiveDivergence;
import plcopen.inf.type.group.sfc.ISimultaneousConvergence;
import plcopen.inf.type.group.sfc.ISimultaneousDivergence;
import plcopen.inf.type.group.sfc.IStep;
import plcopen.inf.type.group.sfc.ITransition;
import plcopen.model.GraphicElement;
/**
 * SFCObject ??? ???? ?? Interface? ???? ???.
 * @author swkim
 *
 */
public class SFCObjectImpl extends GraphicElement implements IStep, IJumpStep,
		IMacroStep, ITransition, ISelectiveDivergence, ISelectiveConvergence,
		ISimultaneousDivergence, ISimultaneousConvergence {

	IBody body;

	ICondition condition;

	List<IConnectionPointIn> connectionIns = new ArrayList<IConnectionPointIn>();

	List<IConnectionPointOut> connectionOuts = new ArrayList<IConnectionPointOut>();

	IConnectionPointIn connectionPointIn;

	IConnectionPointOut connectionPointOut;

	IConnectionPointOut connectionPointOutAction;

	private long executionID;

	boolean initialStep = false;

	private String name = new String();

	boolean negated = false;

	long priority = -1;

	String targetName = new String();

	public IBody getBody() {
		return this.body;
	}

	public ICondition getCondition() {
		return condition;
	}

	public IConnectionPointIn getConnectionPointIn() {
		return connectionPointIn;
	}

	public List<IConnectionPointIn> getConnectionPointIns() {
		return connectionIns;
	}

	public IConnectionPointOut getConnectionPointOut() {
		return connectionPointOut;
	}

	public IConnectionPointOut getConnectionPointOutAction() {
		return connectionPointOutAction;
	}

	public List<IConnectionPointOut> getConnectionPointOuts() {
		return connectionOuts;
	}

	public long getExecutionOrderID() {
		return executionID;
	}

	public String getName() {
		return name;
	}

	public long getPriority() {
		return priority;
	}

	public String getTargetName() {
		return this.targetName;
	}

	public boolean isInitialStep() {
		return initialStep;
	}

	public boolean isNegated() {
		return negated;
	}

	public void setBody(IBody body) {
		this.body = body;
	}

	public void setCondition(ICondition condition) {
		this.condition = condition;
	}

	public void setConnectionPointIn(
			plcopen.inf.type.IConnectionPointIn connectionPointIn) {
		this.connectionPointIn = connectionPointIn;
	}

	public void setConnectionPointIns(List<IConnectionPointIn> list) {
		if (list == null)
			this.connectionIns.clear();
		else
			this.connectionIns = list;
	}

	public void setConnectionPointOut(IConnectionPointOut connectionPointOut) {
		this.connectionPointOut = connectionPointOut;
	}

	public void setConnectionPointOutAction(
			IConnectionPointOut connectionPointOutAction) {
		this.connectionPointOutAction = connectionPointOutAction;
	}

	public void setConnectionPointOuts(List<IConnectionPointOut> list) {
		if (list == null)
			connectionOuts.clear();
		else
			connectionOuts = list;
	}

	public void setExecutionOrderID(long executionOrderID) {
		this.executionID = executionOrderID;
	}

	public void setInitialStep(boolean initialStep) {
		this.initialStep = initialStep;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public void setPriority(long priority) {
		this.priority = priority;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}
}

