package cn.edu.pku.sei.plde.conqueroverfitting.boundary;


import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.boundary.model.BoundaryWithFreq;
import cn.edu.pku.sei.plde.conqueroverfitting.file.ReadFile;
import cn.edu.pku.sei.plde.conqueroverfitting.jdtVisitor.BoundaryCollectVisitor;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.JDTUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoundaryCollect {
    private String rootPath;
    private ArrayList<String> filesPath;
    private ArrayList<BoundaryInfo> boundaryList;

    private boolean isClass;
    public String className;

    public BoundaryCollect(String rootPath, boolean isClass, String className) {
        this.rootPath = rootPath;
        boundaryList = new ArrayList<BoundaryInfo>();
        this.isClass = isClass;
        this.className = className;

        generateBoundaryList();
    }

    private void generateBoundaryList() {
        filesPath = FileUtils.getJavaFilesInProj(rootPath);
        for (String filePath : filesPath) {
            try {
                String source = new ReadFile(filePath).getSource();
                if(source.contains("static") && source.contains("final")){
                    String[] lines = source.split("\\n");
                    for(String line : lines){
                        if(line.contains("static") && line.contains("final")){
                            line = "public class Test { " + line + "}";
                            ASTNode root = JDTUtils.createASTForSource(line, ASTParser.K_COMPILATION_UNIT);
                            if (root == null) {
                                return;
                            }
                            BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
                            root.accept(boundaryCollectVisitor);
                            ArrayList<BoundaryInfo> boundaryListSub = boundaryCollectVisitor.getBoundaryInfoList();
                            for (BoundaryInfo boundaryInfo : boundaryListSub) {
                                boundaryInfo.fileName = filePath;
                            }
//                    System.out.println("ifExpression : " + ifExpression);
//                    System.out.println("boundary : " + boundaryListSub);
                            boundaryList.addAll(boundaryListSub);
                        }
                    }
                }

                Pattern pattern = Pattern.compile("if\\s*\\(.*?\\)");
                Matcher matcher = pattern.matcher(source);
                List<String> result = new ArrayList<String>();
                while(matcher.find()){
                    result.add(matcher.group(0) + "{}");
                }

                for(String ifExpression : result) {

                    ASTNode root = JDTUtils.createASTForSource(ifExpression, ASTParser.K_STATEMENTS);
                    if (root == null) {
                        return;
                    }
                    BoundaryCollectVisitor boundaryCollectVisitor = new BoundaryCollectVisitor();
                    root.accept(boundaryCollectVisitor);
                    ArrayList<BoundaryInfo> boundaryListSub = boundaryCollectVisitor.getBoundaryInfoList();
                    for (BoundaryInfo boundaryInfo : boundaryListSub) {
                        boundaryInfo.fileName = filePath;
                    }
//                    System.out.println("ifExpression : " + ifExpression);
//                    System.out.println("boundary : " + boundaryListSub);
                    boundaryList.addAll(boundaryListSub);
                }
            } catch (NullPointerException e) {
                continue;
            }
        }

//        for(BoundaryInfo boundaryInfo : boundaryList){
//            System.out.println("begin");
//            System.out.println("name: " + boundaryInfo.name);
//            System.out.println("value: " + boundaryInfo.value);
//            System.out.println("type: " + boundaryInfo.variableSimpleType);
//            System.out.println("isSimpleType " + boundaryInfo.isSimpleType);
//            System.out.println("otherType " + boundaryInfo.otherType);
//            System.out.println("variableSimpleType " + boundaryInfo.variableSimpleType);
//            System.out.println("end");
//        }
    }

    public ArrayList<BoundaryInfo> getBoundaryList() {
        return boundaryList;
    }

    public ArrayList<BoundaryWithFreq> getBoundaryWithFreqList() {

        ArrayList<BoundaryWithFreq> boundaryWithFreqs = new ArrayList<BoundaryWithFreq>();
        //System.out.println("size = " + boundaryWithFreqs.size());
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

        if(isClass){
            Iterator<BoundaryWithFreq> iterator = boundaryWithFreqs.iterator();
            while(iterator.hasNext()){
                String value = iterator.next().value;
                if(!value.contains(className) || value.contains("{")){
                    iterator.remove();
                }
            }
        }

        Collections.sort(boundaryWithFreqs, new ComparatorBounaryWithFreqs());

        int size = boundaryWithFreqs.size();

        for(int i = 5; i < size; i ++){
            boundaryWithFreqs.remove(5);
        }

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
        return 1;
    }
}