package cn.edu.pku.sei.plde.conqueroverfitting.visible;

import static org.junit.Assert.*;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.junit.Test;

import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.MethodCollect;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;

public class MethodCollectTest {

	private final String projectPath = "filesfortest";
	private final String suspiciousFilePath = new File(
			"filesfortest/FileForTestMethodCollect.java").getAbsolutePath();
	private final String otherFilePath = new File("filesfortest/FileForTestMethodCollect2.java").getAbsolutePath();

	@Test
	public void testFieldsCollectInClass() {

		MethodCollect methodCollect = MethodCollect.GetInstance(projectPath);

		LinkedHashMap<String, ArrayList<MethodInfo>> methodsInClassMap = methodCollect
				.getVisibleMethodInAllClassMap(suspiciousFilePath);
		assertNotNull(methodsInClassMap);

		assertTrue(methodsInClassMap.containsKey(suspiciousFilePath));
		ArrayList<MethodInfo> methodsInClass = methodsInClassMap
				.get(suspiciousFilePath);
		assertNotNull(methodsInClass);
		assertTrue(methodsInClass.contains((new MethodInfo("test11",
				TypeEnum.BOOLEAN, true, null, true))));
		assertTrue(methodsInClass.contains((new MethodInfo("test12",
				TypeEnum.STRING, true, null, false))));
		
		ArrayList<MethodInfo> methodsInOtherClass = methodsInClassMap
				.get(otherFilePath);
		assertNotNull(methodsInOtherClass);
		assertTrue(methodsInOtherClass.contains((new MethodInfo("test21",
				TypeEnum.BOOLEAN, true, null, true))));
		assertFalse(methodsInOtherClass.contains((new MethodInfo("test22",
				null, false, "FileForTestVariableCollect", false))));
		assertTrue(methodsInOtherClass.contains((new MethodInfo("test23",
				null, false, "FileForTestVariableCollect", true))));
	}
	
	
}
