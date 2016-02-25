package cn.edu.pku.sei.plde.conqueroverfitting.visible.model;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

public class MethodInfo {
	public String methodName;
	public TypeEnum variableSimpleType;
	public boolean isSimpleType;
	public String otherType;
	public boolean isPublic;
	public boolean isStatic;

	public MethodInfo(String methodName, TypeEnum variableSimpleType,
			boolean isSimpleType, String otherType, boolean isPublic, boolean isStatic) {
		this.methodName = methodName;
		this.variableSimpleType = variableSimpleType;
		this.isSimpleType = isSimpleType;
		this.otherType = otherType;
		this.isPublic = isPublic;
		this.isStatic = isStatic;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MethodInfo))
			return false;
		MethodInfo other = (MethodInfo) obj;
		if (isSimpleType != other.isSimpleType)
			return false;
		if (isSimpleType) {
			return methodName.equals(other.methodName)
					&& variableSimpleType.equals(other.variableSimpleType)
					&& isPublic == other.isPublic
					&& isStatic == other.isStatic;
		} else {
			return methodName.equals(other.methodName)
					&& otherType.equals(other.otherType)
					&& isPublic == other.isPublic
					&& isStatic == other.isStatic;
		}
	}
}
