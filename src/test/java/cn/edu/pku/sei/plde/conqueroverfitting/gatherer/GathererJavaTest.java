package cn.edu.pku.sei.plde.conqueroverfitting.gatherer;

import java.util.ArrayList;

import junit.framework.TestCase;
import cn.edu.pku.sei.plde.conqueroverfitting.log.Log;

public class GathererJavaTest extends TestCase {

	public void testGathererJava(){
		ArrayList<String> keyWords = new ArrayList<String>();
		keyWords.add("if");
		keyWords.add("len");
		GathererJava gathererJava = new GathererJava(keyWords, "math");
		gathererJava.searchCode();
	}
}
