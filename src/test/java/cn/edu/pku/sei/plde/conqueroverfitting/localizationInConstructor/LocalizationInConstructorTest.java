package cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor;

import cn.edu.pku.sei.plde.conqueroverfitting.localizationInConstructor.model.ConstructorDeclarationInfo;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Created by yjxxtd on 2/29/16.
 */
public class LocalizationInConstructorTest {

    @Test
    public void testLocalizationInSuperMethod() {
        String testFilePath = "filesfortest//FileForTestLocalizationInConstructorTest.java";
        String testMethodName = "testChromosomeListConstructorTooHigh";
        String projectPah = "filesfortest";

        LocalizationInConstructor localizationInConstructor = new LocalizationInConstructor(projectPah, testFilePath, testMethodName);
        HashMap<String, ArrayList<ConstructorDeclarationInfo>> constructorMap = localizationInConstructor.getConstructorMap();
        assertTrue(constructorMap.size() != 0);
        assertTrue(constructorMap.containsKey("FileForTestLocalizationInConstructor"));
        ArrayList<ConstructorDeclarationInfo> constructorDeclarationList = constructorMap.get("FileForTestLocalizationInConstructor");
        assertTrue(constructorDeclarationList.size() == 2);
        assertTrue(constructorDeclarationList.contains(new ConstructorDeclarationInfo("FileForTestLocalizationInConstructor", 3, 49, 52)));
        assertTrue(constructorDeclarationList.contains(new ConstructorDeclarationInfo("FileForTestLocalizationInConstructor", 2, 63, 66)));
    }
}
