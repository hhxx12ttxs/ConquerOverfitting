package cn.edu.pku.sei.plde.conqueroverfitting.boundary;


import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.BoundaryCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class BoundaryCollect {
    private String rootPath;
    private ArrayList<String> filesPath;
    private ArrayList<BoundaryInfo> boundaryList;

    public BoundaryCollect(String rootPath) {
        this.rootPath = rootPath;
        boundaryList = new ArrayList<BoundaryInfo>();
        generateBoundaryList();
    }

    private void generateBoundaryList() {
        filesPath = FileUtils.getJavaFilesInProj(rootPath);
        for (String filePath : filesPath) {
            try {
                ASTNode root = JDTUtils.createASTForSource(new ReadFile(filePath).getSource(), ASTParser.K_COMPILATION_UNIT);
                if (root == null) {
                    return;
                }
                BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
                root.accept(boundaryCollectVisitor);
                ArrayList<BoundaryInfo> boundaryListSub = boundaryCollectVisitor.getBoundaryInfoList();
                for (BoundaryInfo boundaryInfo : boundaryListSub) {
                    boundaryInfo.fileName = filePath;
                }
                boundaryList.addAll(boundaryListSub);
            } catch (NullPointerException e) {
                continue;
            }
        }
    }

    public ArrayList<BoundaryInfo> getBoundaryList() {
        return boundaryList;
    }

    public ArrayList<BoundaryWithFreq> getBoundaryWithFreqList() {

        ArrayList<BoundaryWithFreq> boundaryWithFreqs = new ArrayList<BoundaryWithFreq>();
        System.out.println("size = " + boundaryWithFreqs.size());
        for (BoundaryInfo boundaryInfo : boundaryList) {
            boolean flag = false;
            for (BoundaryWithFreq boundaryWithFreq : boundaryWithFreqs) {

                if(boundaryWithFreq.value.equals(boundaryInfo.value)){

                    boundaryWithFreq.freq++;
                    boundaryWithFreq.leftClose += boundaryInfo.leftClose;
                    boundaryWithFreq.rightClose += boundaryInfo.rightClose;
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                boundaryWithFreqs.add(new BoundaryWithFreq(boundaryInfo.variableSimpleType, boundaryInfo.isSimpleType,
                        boundaryInfo.otherType, boundaryInfo.value, boundaryInfo.leftClose, boundaryInfo.rightClose, 1));
            }
        }

        Collections.sort(boundaryWithFreqs, new ComparatorBounaryWithFreqs());

//        int size = boundaryWithFreqs.size();
//
//        for(int i = 100; i < size; i ++){
//            boundaryWithFreqs.remove(100);
//        }

        return boundaryWithFreqs;
    }
}

class ComparatorBounaryWithFreqs implements Comparator {
    @Override
    public int compare(Object arg0, Object arg1) {

        BoundaryWithFreq boundaryWithFreq0 = (BoundaryWithFreq) arg0;
        BoundaryWithFreq boundaryWithFreq1 = (BoundaryWithFreq) arg1;
            if (boundaryWithFreq0.freq > boundaryWithFreq1.freq) {
                return -1;
            }
            if (boundaryWithFreq0.freq == boundaryWithFreq1.freq) {
                return 0;
            }
        return -1;
    }
}