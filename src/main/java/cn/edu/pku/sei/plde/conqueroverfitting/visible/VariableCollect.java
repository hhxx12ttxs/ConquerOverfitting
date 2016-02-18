package cn.edu.pku.sei.plde.conqueroverfitting.visible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import cn.edu.pku.sei.plde.conqueroverfitting.jdt.JDTParse;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

public class VariableCollect {
	private static VariableCollect instance;

	private static LinkedHashMap<String, ArrayList<VariableInfo>> filedsInClassMap;
	private static LinkedHashMap<String, ArrayList<VariableInfo>> parameterInMethodMap;
	private static LinkedHashMap<String, ArrayList<VariableInfo>> localInMethodMap;
  
	public VariableCollect(String projectPath) {
		getAllVisibleVariable(projectPath);
	}

	public static VariableCollect GetInstance(String projectPath) {
		if (instance == null) {
			synchronized (VariableCollect.class) {
				instance = new VariableCollect(projectPath);
			}
		}
		return instance;
	}

	public static void getAllVisibleVariable(String projectPath) {
		filedsInClassMap = new LinkedHashMap<String, ArrayList<VariableInfo>>();
		parameterInMethodMap = new LinkedHashMap<String, ArrayList<VariableInfo>>();
		localInMethodMap = new LinkedHashMap<String, ArrayList<VariableInfo>>();
		ArrayList<String> filesPath = FileUtils.getJavaFilesInProj(projectPath);
		for (String filePath : filesPath) {
			JDTParse jdtParse = new JDTParse(filePath);
			filedsInClassMap.put(filePath, jdtParse.getFieldInClassList());
			parameterInMethodMap.put(filePath,
					jdtParse.getParameterInMethodList());
			localInMethodMap.put(filePath, jdtParse.getLocalInMethodList());
		}
	}

	public LinkedHashMap<String, ArrayList<VariableInfo>> getVisibleFieldInAllClassMap(
			String sourcePath) {
		LinkedHashMap<String, ArrayList<VariableInfo>> fieldsInClassMapRet = new LinkedHashMap<String, ArrayList<VariableInfo>>(filedsInClassMap);
		for (Map.Entry<String, ArrayList<VariableInfo>> entry : fieldsInClassMapRet
				.entrySet()) {
			String filePath = entry.getKey();
			if(filePath.equals(sourcePath)){
				continue;
			}
			ArrayList<VariableInfo> fieldsInClassList = entry.getValue();
			for (Iterator<VariableInfo> it = fieldsInClassList.iterator(); it
					.hasNext();) {
				VariableInfo variableInfo = it.next();
				if (!variableInfo.isPublic) {
					it.remove();
				}
			}
		}
		return fieldsInClassMapRet;
	}

	public ArrayList<VariableInfo> getVisibleParametersInMethodList(
			String sourcePath, int suspiciousLineNum) {
		ArrayList<VariableInfo> parameters = new ArrayList<VariableInfo>();
		if (parameterInMethodMap == null
				|| !parameterInMethodMap.containsKey(sourcePath))
			return parameters;

		ArrayList<VariableInfo> parametersInMethods = parameterInMethodMap
				.get(sourcePath);
		for (VariableInfo variableInfo : parametersInMethods) {
			if (variableInfo.methodEndPos < suspiciousLineNum)
				continue;
			if (variableInfo.methodStartPos < suspiciousLineNum
					&& variableInfo.methodEndPos > suspiciousLineNum) {
				parameters.add(variableInfo);
			}
			if (variableInfo.methodStartPos > suspiciousLineNum)
				break;
		}

		return parameters;
	}

	public ArrayList<VariableInfo> getVisibleLocalInMethodList(
			String sourcePath, int suspiciousLineNum) {
		ArrayList<VariableInfo> locals = new ArrayList<VariableInfo>();

		if (localInMethodMap == null
				|| !localInMethodMap.containsKey(sourcePath))
			return locals;

		ArrayList<VariableInfo> localsInMethods = localInMethodMap
				.get(sourcePath);
		for (VariableInfo variableInfo : localsInMethods) {
			if (variableInfo.methodEndPos < suspiciousLineNum)
				continue;
			if (variableInfo.methodStartPos < suspiciousLineNum
					&& variableInfo.methodEndPos > suspiciousLineNum
					&& variableInfo.variablePos < suspiciousLineNum) {
				locals.add(variableInfo);
			}
			if (variableInfo.methodStartPos > suspiciousLineNum)
				break;
		}

		return locals;
	}
}
