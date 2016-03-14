package cn.edu.pku.sei.plde.conqueroverfitting.boundary;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.log.Log;
import cn.edu.pku.sei.plde.conqueroverfitting.type.TypeEnum;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class BoundaryCollectTest {

    @Test
    public void testBoundaryCollect() {
        String path = "filesfortest";
        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
        ArrayList<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();

        //assertNotNull(boundaryList);
        //assertTrue(boundaryList.contains(new BoundaryInfo(TypeEnum.INT, true, null, "a", "1", null)));
        //assertTrue(boundaryList.contains(new BoundaryInfo(TypeEnum.STRING, true, null, "f", "\"ww\"", null)));
        assertTrue(boundaryList.contains(new BoundaryInfo(TypeEnum.NULL, true, null, "fileForTestBoundaryCollect2", "null", null)));
		Log log = new Log("log//boundary.log");
		for(BoundaryInfo boundaryInfo : boundaryList){
			log.logSignLine("begin");
			log.logStr("name: " + boundaryInfo.info);
			log.logStr("value: " + boundaryInfo.value);
			log.logStr("type: " + boundaryInfo.variableSimpleType);
			log.logSignLine("end");
		}
    }


    @Test
    public void testMath26() {
        String path = "experiment//searchcode//math-long-a0";
        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
        ArrayList<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();

        assertNotNull(boundaryList);
		Log log = new Log("log//boundary-math-long-a0.log");
		for(BoundaryInfo boundaryInfo : boundaryList){
			log.logSignLine("begin");
			log.logStr("name: " + boundaryInfo.info);
			log.logStr("value: " + boundaryInfo.value);
			log.logStr("type: " + boundaryInfo.variableSimpleType);
			log.logSignLine("end");
		}
    }

    @Test
    public void testMath99() {
        String path = "experiment//searchcode//math-int-u";
        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
        ArrayList<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();

        assertNotNull(boundaryList);
        Log log = new Log("log//boundary-math-int-u.log");
        for(BoundaryInfo boundaryInfo : boundaryList){
            log.logSignLine("begin");
            log.logStr("name: " + boundaryInfo.info);
            log.logStr("value: " + boundaryInfo.value);
            log.logStr("type: " + boundaryInfo.variableSimpleType);
            log.logSignLine("end");
        }
    }

    @Test
    public void testTime9() {
        String path = "experiment//searchcode//time-int-millisOffset";
        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
        ArrayList<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();

        assertNotNull(boundaryList);
        Log log = new Log("log//boundary-time-int-millisOffset.log");
        for(BoundaryInfo boundaryInfo : boundaryList){
            log.logSignLine("begin");
            log.logStr("name: " + boundaryInfo.info);
            log.logStr("value: " + boundaryInfo.value);
            log.logStr("type: " + boundaryInfo.variableSimpleType);
            log.logSignLine("end");
        }
    }

    @Test
    public void testMath15() {
        String path = "experiment//searchcode//math-double-pow";
        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
        ArrayList<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();

        assertNotNull(boundaryList);
        Log log = new Log("log//boundary-math-double-pow.log");
        for(BoundaryInfo boundaryInfo : boundaryList){
            log.logSignLine("begin");
            log.logStr("name: " + boundaryInfo.info);
            log.logStr("value: " + boundaryInfo.value);
            log.logStr("type: " + boundaryInfo.variableSimpleType);
            log.logSignLine("end");
        }
    }

    @Test
    public void testMath37() {
        String path = "experiment//searchcode//math-double-real";
        BoundaryCollect boundaryCollect = new BoundaryCollect(path);
        ArrayList<BoundaryInfo> boundaryList = boundaryCollect.getBoundaryList();

        assertNotNull(boundaryList);
        Log log = new Log("log//boundary-math-double-real.log");
        for(BoundaryInfo boundaryInfo : boundaryList){
            log.logSignLine("begin");
            log.logStr("name: " + boundaryInfo.info);
            log.logStr("value: " + boundaryInfo.value);
            log.logStr("type: " + boundaryInfo.variableSimpleType);
            log.logSignLine("end");
        }
    }
}
