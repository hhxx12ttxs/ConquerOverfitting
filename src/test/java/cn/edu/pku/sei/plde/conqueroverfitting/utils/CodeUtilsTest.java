package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import org.junit.Test;
import static org.junit.Assert.*;
import java.util.HashSet;
import java.util.Set;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.IdentifierCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.junit.Test;
/**
 * Created by yjxxtd on 4/26/16.
 */
public class CodeUtilsTest {
    @Test
    public void testGetVariableInMethod(){
        String methodSrc = "\tpublic int method(int c){\n" +
                "\t\tint a = f(d);\n" +
                "\t\treturn b.e;\n" +
                "\t}";
        Set<String> suspiciousVariables = new HashSet<String>();
        suspiciousVariables.add("a");
        suspiciousVariables.add("g");
        CodeUtils.getVariableInMethod(methodSrc, suspiciousVariables);
        assertTrue(suspiciousVariables.size() == 1);
        assertTrue(suspiciousVariables.contains("a"));
    }
}
