package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.agent.RunTestAgent;
import cn.edu.pku.sei.plde.conqueroverfitting.agent.Utils;
import cn.edu.pku.sei.plde.conqueroverfitting.assertCollect.Asserts;
import cn.edu.pku.sei.plde.conqueroverfitting.junit.JunitRunner;
import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.CodeUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.FileUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.PathUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.utils.ShellUtils;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.MethodInfo;
import cn.edu.pku.sei.plde.conqueroverfitting.visible.model.VariableInfo;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.internal.compiler.ast.OR_OR_Expression;
import org.junit.runner.JUnitCore;
import sun.misc.BASE64Encoder;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yanrunfa on 16/2/24.
 */
public class MethodOneFixer {
    private final String _classpath;
    private final String _testClassPath;
    private final String _classSrcPath;
    private final String _testSrcPath;
    private Suspicious _suspicious;
    private List<Patch> _patches = new ArrayList<>();

    public MethodOneFixer(Suspicious suspicious){
        _suspicious = suspicious;
        _classpath = suspicious._classpath;
        _testClassPath = suspicious._testClasspath;
        _classSrcPath = suspicious._srcPath;
        _testSrcPath = suspicious._testSrcPath;
    }

    public boolean addPatch(Patch patch){
        File targetJavaFile = new File(FileUtils.getFileAddressOfJava(_classSrcPath, patch._className));
        File targetClassFile = new File(FileUtils.getFileAddressOfClass(_classpath, patch._className));
        File javaBackup = FileUtils.copyFile(targetJavaFile.getAbsolutePath(), FileUtils.tempJavaPath(patch._className,"MethodOneFixer"));
        File classBackup = FileUtils.copyFile(targetClassFile.getAbsolutePath(), FileUtils.tempClassPath(patch._className,"MethodOneFixer"));

        String truePatchString = "";
        int truePatchLine = -1;
        for (String patchString: patch._patchString){
            for (int patchLine: patch._patchLines){
                FileUtils.copyFile(javaBackup, targetJavaFile);
                CodeUtils.addCodeToFile(targetJavaFile, patchString, patchLine);

                if (!patch._addonFunction.equals("")){
                    CodeUtils.addMethodToFile(targetJavaFile, patch._addonFunction, patch._className.substring(patch._className.lastIndexOf(".")+1));
                }
                try {
                    targetClassFile.delete();
                    System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_classpath+" "+ targetJavaFile.getAbsolutePath())));
                }
                catch (IOException e){
                    continue;
                }
                if (!targetClassFile.exists()){ //编译不成功
                    continue;
                }
                Asserts asserts = new Asserts(_classpath,_classSrcPath, _testClassPath, _testSrcPath, patch._testClassName, patch._testMethodName);
                int errAssertNumAfterFix = asserts.errorNum();
                int errAssertBeforeFix = _suspicious._assertsMap.get(patch._testClassName+"#"+patch._testMethodName).errorNum();


                System.out.println(patchString);
                if (errAssertNumAfterFix < errAssertBeforeFix){
                    truePatchLine = patchLine;
                    truePatchString = patchString;
                    break;
                }
            }
        }
        FileUtils.copyFile(classBackup, targetClassFile);
        if (truePatchLine!=-1 && !truePatchString.equals("")){
            patch._patchString.clear();
            patch._patchString.add(truePatchString);
            patch._patchLines.clear();
            patch._patchLines.add(truePatchLine);
            return true;
        }
        return false;
    }


    public int fix(){
        if (_patches.size() == 0){
            return -1;
        }
        Map<File, File> backups = new HashMap<>();
        List<File> tobeCompile = new ArrayList<>();
        for (Patch patch: _patches){
            File targetJavaFile = new File(FileUtils.getFileAddressOfJava(_classSrcPath, patch._className));
            File targetClassFile = new File(FileUtils.getFileAddressOfClass(_classpath, patch._className));
            File javaBackup = FileUtils.copyFile(targetJavaFile.getAbsolutePath(), FileUtils.tempJavaPath(patch._className,"MethodOneFixer"));
            File classBackup = FileUtils.copyFile(targetClassFile.getAbsolutePath(), FileUtils.tempClassPath(patch._className,"MethodOneFixer"));
            if (!backups.containsKey(targetJavaFile)){
                backups.put(targetJavaFile, javaBackup);
            }
            if (!backups.containsKey(targetClassFile)){
                backups.put(targetClassFile, classBackup);
            }
            CodeUtils.addCodeToFile(targetJavaFile, patch._patchString.get(0), patch._patchLines);
            tobeCompile.add(targetJavaFile);
        }
        for (File javaFile: tobeCompile){
            try {
                System.out.println(Utils.shellRun(Arrays.asList("javac -Xlint:unchecked -source 1.6 -target 1.6 -cp "+ buildClasspath(Arrays.asList(PathUtils.getJunitPath())) +" -d "+_classpath+" "+ javaFile.getAbsolutePath())));
            }
            catch (IOException e){
                return -1;
            }
        }
        int errAssertNumAfterFix = 0;
        for (Map.Entry<String, String> test: getTestsOfPatch().entrySet()){
            Asserts asserts = new Asserts(_classpath,_classSrcPath, _testClassPath, _testSrcPath, test.getValue(), test.getKey());
            errAssertNumAfterFix += asserts.errorAssertNum();

        }
        int errAssertNumBeforeFix = _suspicious.errorAssertNums();
        if (errAssertNumAfterFix < errAssertNumBeforeFix){
            return errAssertNumAfterFix;
        }
        for (Map.Entry<File, File> backup: backups.entrySet()){
            FileUtils.copyFile(backup.getValue(), backup.getKey());
        }
        return -1;
    }


    private Map<String, String> getTestsOfPatch(){
        Map<String, String> result = new HashMap<>();
        for (Patch patch: _patches){
            if (!result.containsKey(patch._testMethodName)){
                result.put(patch._testMethodName, patch._testClassName);
            }
        }
        return result;
    }


    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += System.getProperty("path.separator");
        path += JunitRunner.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    /*
    private String fixShell(String testClassname, String classname, List<Integer> errorLine, String patch) throws IOException {
        String agentArg = buildAgentArg(classname, errorLine, patch);
        String classpath = buildClasspath(Arrays.asList(PathUtils.getJunitPath()));
        String[] arg = {"java","-javaagent:"+PathUtils.getAgentPath()+"="+agentArg,"-cp",classpath,"org.junit.runner.JUnitCore", testClassname.replace("-"," ")};
        String shellResult = ShellUtils.shellRun(Arrays.asList(StringUtils.join(arg, " ")));
        if (shellResult.length() <= 0){
            throw new IOException("Shell Run Error, Shell Args:"+StringUtils.join(arg," "));
        }
        return shellResult;
    }

    private String buildAgentArg(String classname, List<Integer> errorLine, String patch) throws IOException{
        String agentClass = "class:"+ classname;
        String agentLine = "line:"+ StringUtils.join(errorLine,"-");
        String agentSrc = "src:" + _classSrcPath;
        String agentCp = "cp:" + _classpath;
        BASE64Encoder encoder = new BASE64Encoder();
        String agentPatch = "patch:" + encoder.encode(patch.getBytes("utf-8"));
        if (agentPatch.endsWith("=")){
            agentPatch = agentPatch.substring(0, agentPatch.length()-2).replace("\n","");
        }
        return "\""+StringUtils.join(Arrays.asList(agentClass,agentLine,agentSrc,agentCp,agentPatch),",")+"\"";
    }

    private String buildClasspath(List<String> additionalPath){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += System.getProperty("path.separator");
        path += StringUtils.join(additionalPath,System.getProperty("path.separator"));
        path += "\"";
        return path;
    }

    private String buildClasspath(){
        String path = "\"";
        path += _classpath;
        path += System.getProperty("path.separator");
        path += _testClassPath;
        path += "\"";
        return path;
    }
    */
}
