package cn.edu.pku.sei.plde.conqueroverfitting.boundary;


import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTParser;

import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.jdt.JDTParse;
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
			JDTParse jdtParse = new JDTParse(new ReadFile(filePath).getSource(), ASTParser.K_COMPILATION_UNIT);
			boundaryList.addAll(jdtParse.getBoundaryList());
		}
		return boundaryList;
	}
}
