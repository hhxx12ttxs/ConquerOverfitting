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
		keyWords.add("int");
		keyWords.add("hours");
		GathererJava gathererJava = new GathererJava(keyWords, "joda-time");
		//new GathererJava(keyWords, "commons-math");Math
		//new GathererJava(keyWords, "commons-lang");Lang
		//new GathererJava(keyWords, "closure-compiler");Closure
		//new GathererJava(keyWords, "jfreechart");Chart
		//new GathererJava(keyWords, "joda-time");Time
		gathererJava.searchCode();
		long timeEnd = System.currentTimeMillis();
		//103s
		System.out.println("time = " + (timeEnd - timeStart)/1000 );
	}
}
