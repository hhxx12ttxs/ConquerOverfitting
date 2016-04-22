package cn.edu.pku.sei.plde.conqueroverfitting.gatherer;

import java.util.ArrayList;

import junit.framework.TestCase;
import cn.edu.pku.sei.plde.conqueroverfitting.log.Log;
import org.junit.Test;

public class GathererJavaTest extends TestCase {

    @Test
	public void testGathererJavaGithub(){
		long timeStart = System.currentTimeMillis();
		ArrayList<String> keyWords = new ArrayList<String>();
		keyWords.add("if");
		//keyWords.add("int");
		keyWords.add("primitiveType");
		//keyWords.add("int");
		GathererJavaGithub gathererJava = new GathererJavaGithub(keyWords, "if-primitiveType", "commons-lang");
		//new GathererJavaGithub(keyWords, "commons-math");Math
		//new GathererJavaGithub(keyWords, "commons-lang");Lang
		//new GathererJavaGithub(keyWords, "closure-compiler");Closure
		//new GathererJavaGithub(keyWords, "jfreechart");Chart
		//new GathererJavaGithub(keyWords, "joda-time");Time
		//GathererJavaGithub gathererJavaGithub = new GathererJavaGithub(keyWords, "if-Complex","math-complex");
		gathererJava.searchCode();
		long timeEnd = System.currentTimeMillis();
		//103s
		System.out.println("time = " + (timeEnd - timeStart)/1000 );
	}
}
