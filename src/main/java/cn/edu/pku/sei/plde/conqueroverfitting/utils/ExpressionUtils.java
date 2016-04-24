package cn.edu.pku.sei.plde.conqueroverfitting.utils;

import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.ExpressionCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.IdentifierCollectVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by yjxxtd on 4/24/16.
 */
public class ExpressionUtils {
    public static Set<String> getExpressionsInMethod(String source){
        ASTNode root = JDTUtils.createASTForSource(source, ASTParser.K_CLASS_BODY_DECLARATIONS);
        ExpressionCollectVisitor expressionCollectVisitor = new ExpressionCollectVisitor();
        root.accept(expressionCollectVisitor);
        Set<String> expressionSet = expressionCollectVisitor.getExpressionSet();
        Iterator<String> it = expressionSet.iterator();
        Set<String> removeSet = new HashSet<String>();

        while(it.hasNext()){
            String expr = it.next();
            if(expr.contains(">") || expr.contains("==") || expr.contains("<")){
                removeSet.add(expr);
            }
        }

        expressionSet.removeAll(removeSet);
        removeSet.clear();;

        it = expressionSet.iterator();
        while(it.hasNext()){
            String expr = it.next();
            Iterator<String> it2 = expressionSet.iterator();
            while(it2.hasNext()){
                String expr2 = it2.next();
                if(!expr.equals(expr2) && expr2.contains(expr)){
                    removeSet.add(expr);
                }
            }
        }

        expressionSet.removeAll(removeSet);
        removeSet.clear();

        it = expressionSet.iterator();
        while(it.hasNext()){
            String expr = it.next();
            if(expr.contains("=")){
                removeSet.add(expr);
            }
        }

        expressionSet.removeAll(removeSet);
        removeSet.clear();;


        return expressionCollectVisitor.getExpressionSet();
    }
}
