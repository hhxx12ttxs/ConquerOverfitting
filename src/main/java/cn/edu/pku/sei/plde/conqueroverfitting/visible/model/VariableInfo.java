package cn.edu.pku.sei.plde.conqueroverfitting.visible.model;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

import java.io.Serializable;

public class VariableInfo implements Comparable<VariableInfo>,Serializable{
	public String variableName;
	public TypeEnum variableSimpleType;
	public boolean isSimpleType;
	public String otherType;
    public boolean isPublic;
    public int methodStartPos;
    public int methodEndPos;
    public int variablePos;

	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
	 * @param methodStartPos
	 * @param methodEndPos
     * @param variablePos
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType, int methodStartPos, int methodEndPos, int variablePos) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.methodStartPos = methodStartPos;
		this.methodEndPos = methodEndPos;
		this.variablePos = variablePos;
	}

	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
	 * @param methodStartPos
     * @param methodEndPos
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType, int methodStartPos, int methodEndPos) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.methodStartPos = methodStartPos;
		this.methodEndPos = methodEndPos;
	}

	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
     * @param isPublic
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType, boolean isPublic) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.isPublic = isPublic;
	}

	/**
	 *
	 * @param variableName
	 * @param variableSimpleType
	 * @param isSimpleType
	 * @param otherType
     */
	public VariableInfo(String variableName,
			TypeEnum variableSimpleType, boolean isSimpleType,
			String otherType) {
		this.variableName = variableName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof VariableInfo))
			return false;
		VariableInfo other = (VariableInfo) obj;
		if (isSimpleType != other.isSimpleType)
			return false;
		if (isSimpleType) {
			return variableName.equals(other.variableName)
					&& variableSimpleType.equals(other.variableSimpleType)
					&& isPublic == other.isPublic;
		} else {
			return variableName.equals(other.variableName)
					&& otherType.equals(other.otherType)
					&& isPublic == other.isPublic;
		}
	}

	public int compareTo(VariableInfo variableInfo) {
		return methodStartPos - variableInfo.methodStartPos;
	}   
}
