package cn.edu.pku.sei.plde.conqueroverfitting.gatherer;

import java.util.ArrayList;

import junit.framework.TestCase;
import cn.edu.pku.sei.plde.conqueroverfitting.log.Log;
import org.junit.Test;

public class GathererJavaTest extends TestCase {

    @Test
	public void testGathererJava(){
		long timeStart = System.currentTimeMillis();
		ArrayList<String> keyWords = new ArrayList<String>();
		keyWords.add("if");
		keyWords.add("Complex");
		//keyWords.add("divide");
		GathererJava gathererJava = new GathererJava(keyWords, "math-complex");
		gathererJava.searchCode();
		long timeEnd = System.currentTimeMillis();
		//103s
		System.out.println("time = " + (timeEnd - timeStart)/1000 );
	}
}
