package cn.edu.pku.sei.plde.conqueroverfitting.visible;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTParser;

import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.jdt.JDTParse;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;

public class MethodCollect {
	private static MethodCollect instance;

	private static LinkedHashMap<String, ArrayList<MethodInfo>> methodsInClassMap;
  
	public MethodCollect(String projectPath) {
		getAllVisibleMethod(projectPath);
	}

	public static MethodCollect GetInstance(String projectPath) {
		if (instance == null) {
			synchronized (MethodCollect.class) {
				instance = new MethodCollect(projectPath);
			}
		}
		return instance;
	}

	public static void getAllVisibleMethod(String projectPath) {
		methodsInClassMap = new LinkedHashMap<String, ArrayList<MethodInfo>>();
		ArrayList<String> filesPath = FileUtils.getJavaFilesInProj(projectPath);
		for (String filePath : filesPath) {
			JDTParse jdtParse = new JDTParse(new ReadFile(filePath).getSource(), ASTParser.K_COMPILATION_UNIT);
			methodsInClassMap.put(filePath, jdtParse.getMethodInClassList());
		}
	}

	public static boolean checkIsStaticMethod(String sourcePath, String methodName){
		if(!methodsInClassMap.containsKey(sourcePath)){
			return false;
		}
		ArrayList<MethodInfo> methodInfos = methodsInClassMap.get(sourcePath);
		for(MethodInfo methodInfo : methodInfos){
			if(methodInfo.methodName.equals(methodName)){
				return methodInfo.isStatic;
			}
		}
		return false;
	}

	public LinkedHashMap<String, ArrayList<MethodInfo>> getVisibleMethodInAllClassMap(
			String sourcePath) {
		LinkedHashMap<String, ArrayList<MethodInfo>> methodsInClassMapRet = new LinkedHashMap<String, ArrayList<MethodInfo>>(methodsInClassMap);
		for (Map.Entry<String, ArrayList<MethodInfo>> entry : methodsInClassMapRet
				.entrySet()) {
			String filePath = entry.getKey();
			if(filePath.equals(sourcePath)){
				continue;
			}
			ArrayList<MethodInfo> methodsInClassList = entry.getValue();
			for (Iterator<MethodInfo> it = methodsInClassList.iterator(); it
					.hasNext();) {
				MethodInfo methodInfo = it.next();
				if (!methodInfo.isPublic) {
					it.remove();
				}
			}
		}
		return methodsInClassMapRet;
	}
}
