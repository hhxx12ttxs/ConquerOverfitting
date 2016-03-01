package cn.edu.pku.sei.plde.conqueroverfitting.boundary;


import java.util.ArrayList;

import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.BoundaryCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;

public class BoundaryCollect {
	private String rootPath;
	private ArrayList<String> filesPath;
	private ArrayList<BoundaryInfo> boundaryList;
    
	public BoundaryCollect(String rootPath) {
		this.rootPath = rootPath;
		boundaryList = new ArrayList<BoundaryInfo>();
	}

	public ArrayList<BoundaryInfo> getBoundaryList(){
		filesPath = FileUtils.getJavaFilesInProj(rootPath);
		for(String filePath : filesPath){
			ASTNode root = JDTUtils.createASTForSource(new ReadFile(filePath).getSource(), ASTParser.K_COMPILATION_UNIT);
			BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
			root.accept(boundaryCollectVisitor);

			boundaryList.addAll(boundaryCollectVisitor.getBoundaryInfoList());
		}
		return boundaryList;
	}
}
