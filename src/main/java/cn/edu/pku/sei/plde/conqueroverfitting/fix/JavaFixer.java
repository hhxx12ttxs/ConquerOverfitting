package cn.edu.pku.sei.plde.conqueroverfitting.fix;

import cn.edu.pku.sei.plde.conqueroverfitting.localization.Suspicious;

import java.util.List;

/**
 * Created by yanrunfa on 16-4-13.
 */
public class JavaFixer {
    public static boolean fixMethodTwo(Suspicious suspicious, List<String> ifStrings, String project) throws Exception{
        MethodTwoFixer fixer = new MethodTwoFixer(suspicious);
        if (fixer.fix(ifStrings)){
            return true;
        }
        else {
            System.out.println("Fix fail, Try next suspicious...");
            return false;
        }
    }


    public static boolean fixMethodOne(Suspicious suspicious, List<String> ifStrings, String project) throws Exception{
        ReturnCapturer fixCapturer = new ReturnCapturer(suspicious._classpath,suspicious._srcPath, suspicious._testClasspath, suspicious._testSrcPath);
        MethodOneFixer methodOneFixer = new MethodOneFixer(suspicious, project);
        boolean isSuccess = false;
        for (String test: suspicious.getFailedTest()){
            String testClassName = test.split("#")[0];
            String testMethodName = test.split("#")[1];
            List<Integer> errorLine = suspicious._errorLineMap.get(test);

            if (suspicious._assertsMap.get(test)._errorAssertLines.size() == 0){
                suspicious._assertsMap.get(test)._errorAssertLines.add(-1);
            }
            for (int assertLine: suspicious._assertsMap.get(test)._errorAssertLines){
                String fixString = fixCapturer.getFixFrom(testClassName, testMethodName, assertLine, suspicious.classname(), suspicious.functionnameWithoutParam());
                if (suspicious._isConstructor && fixString.contains("return")){
                    continue;
                }
                if (fixString.equals("")){
                    continue;
                }
                Patch patch = new Patch(testClassName, testMethodName, suspicious.classname(), errorLine, ifStrings, fixString);
                boolean result = methodOneFixer.addPatch(patch);
                if (result){
                    isSuccess = true;
                    break;
                }
            }
            if (isSuccess){
                break;
            }
        }
        int finalErrorNums = methodOneFixer.fix();
        if (finalErrorNums == -1){
            System.out.println("Fix fail, Try next suspicious...");
            return false;
        }
        if (finalErrorNums == 0){
            System.out.println("Fix success");
            return true;
        }
        return false;
    }
}
