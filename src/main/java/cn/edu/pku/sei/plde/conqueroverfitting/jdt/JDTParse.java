package cn.edu.pku.sei.plde.conqueroverfitting.jdt;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
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
	private int[] lineCounter;
    
	public JDTParse(String sourcePath) {
		ASTNode root = createASTForSource(sourcePath,
				ASTParser.K_COMPILATION_UNIT);

		VariableCollectVisitor variableCollectVisitor = new VariableCollectVisitor(
				lineCounter);
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
	}
	

	private String readAndProcessSource(String sourcePath) {
		String content = "";
		try {
			FileReader fr = new FileReader(sourcePath);
			BufferedReader br = new BufferedReader(fr);

			String line = "";
			line = br.readLine();
			while (line != null) {
				line = line.replaceAll("&nbsp;", " ");
				line = line.replaceAll("\t", " ");
				line = line.replaceAll("\\s{2,100}", " ");
				content = content + line + "\r\n";
				line = br.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		lineCounter = new int[content.length()];
		int i = 0;
		lineCounter[0] = 1;
		int CurrentLine = 1;
		for (i = 0; i < content.length(); i++) {
			if (content.charAt(i) == '\r') {
				lineCounter[i] = CurrentLine;
				lineCounter[i + 1] = CurrentLine;
				CurrentLine++;
				i = i + 1;
				continue;
			} else {
				lineCounter[i] = CurrentLine;
			}
		}

		return content;
	}

	private ASTNode createASTForSource(String sourcePath, int kind) {

		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource(readAndProcessSource(sourcePath).toCharArray());
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
}
