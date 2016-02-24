package cn.edu.pku.sei.plde.conqueroverfitting.jdt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;

/**
 * Useful functions on JDT
 * 
 * @author jiewang
 */
public class JDTParse {

	private ArrayList<BoundaryInfo> boundaryList;
	private ArrayList<VariableInfo> filedInClassList;
	private ArrayList<VariableInfo> parameterInMethodList;
	private ArrayList<VariableInfo> localInMethodList;
	private ArrayList<MethodInfo> methodInClassList;
	private ArrayList<String> identifierList;
	private int[] lineCounter;

	public JDTParse(String source, int kind) {

		ASTNode root = createASTForSource(source, kind);

		if (kind == ASTParser.K_COMPILATION_UNIT) {
			processSource(source);
			VariableCollectVisitor variableCollectVisitor = new VariableCollectVisitor(lineCounter);
			BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
			root.accept(variableCollectVisitor);
			root.accept(boundaryCollectVisitor);

			boundaryList = boundaryCollectVisitor.getBoundaryInfoList();
			filedInClassList = variableCollectVisitor.getFiledsInClassList();
			parameterInMethodList = variableCollectVisitor.getParametersInMethodList();
			localInMethodList = variableCollectVisitor.getLocalInMethodList();
			methodInClassList = variableCollectVisitor.getMethodsInClassList();
			Collections.sort(parameterInMethodList);
			Collections.sort(localInMethodList);
		} else if (kind == ASTParser.K_EXPRESSION || kind == ASTParser.K_STATEMENTS) {
			IdentifierCollectVisitor identifierCollectVisitor = new IdentifierCollectVisitor();
			root.accept(identifierCollectVisitor);

			identifierList = identifierCollectVisitor.getIdentifierList();
		}
	}

	private void processSource(String source) {
		lineCounter = new int[source.length()];
		int i = 0;
		lineCounter[0] = 1;
		int CurrentLine = 1;
		for (i = 0; i < source.length(); i++) {
			if (source.charAt(i) == '\r') {
				lineCounter[i] = CurrentLine;
				lineCounter[i + 1] = CurrentLine;
				CurrentLine++;
				i = i + 1;
				continue;
			} else {
				lineCounter[i] = CurrentLine;
			}
		}
	}

	private ASTNode createASTForSource(String source, int kind) {

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(source.toCharArray());
		parser.setKind(kind);
		parser.setResolveBindings(true);
		Map options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);
		return parser.createAST(null);
	}

	public ArrayList<BoundaryInfo> getBoundaryList() {
		return boundaryList;
	}

	public ArrayList<VariableInfo> getFieldInClassList() {
		return filedInClassList;
	}

	public ArrayList<VariableInfo> getParameterInMethodList() {
		return parameterInMethodList;
	}

	public ArrayList<VariableInfo> getLocalInMethodList() {
		return localInMethodList;
	}

	public ArrayList<MethodInfo> getMethodInClassList() {
		return methodInClassList;
	}

	public ArrayList<String> getIdentifierList() {
		return identifierList;
	}
}
