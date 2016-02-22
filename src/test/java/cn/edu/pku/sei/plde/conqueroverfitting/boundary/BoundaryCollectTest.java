package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.log.Log;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;

public class BoundaryCollectTest {
	private final String projectPath = "filesfortest";
	
	@Test
	public void testBoundaryCollect(){
		BoundaryCollect boundaryCollect = new BoundaryCollect(projectPath);
		ArrayList<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();
		
		assertNotNull(boundaryList);
		assertTrue(boundaryList.contains(new BoundaryInfo(TypeEnum.INT, true, null, "a", "1", null)));
		assertTrue(boundaryList.contains(new BoundaryInfo(TypeEnum.STRING, true, null, "f", "\"ww\"", null)));
		assertTrue(boundaryList.contains(new BoundaryInfo(TypeEnum.NULL, true, null, "fileForTestBoundaryCollect2", "null", null)));
//		Log log = new Log("log//boundary.log");
//		for(BoundaryInfo boundaryInfo : boundaryInfos){
//			log.logSignLine("begin");
//			log.logStr("name: " + boundaryInfo.info);
//			log.logStr("value: " + boundaryInfo.value);
//			log.logStr("type: " + boundaryInfo.variableSimpleType);
//			log.logSignLine("end");
//		}
	}
}
