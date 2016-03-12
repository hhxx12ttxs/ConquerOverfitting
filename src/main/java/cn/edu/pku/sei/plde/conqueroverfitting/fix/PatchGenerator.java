package cn.edu.pku.sei.plde.conqueroverfitting.fix;

/**
 * Created by yanrunfa on 16/3/11.
 */
public class PatchGenerator {
    public static String generate(String ifString, String fixString){
        String patch = "";
        ifString = ifString.replace("(int)-2.147483648E9","Integer.MIN_VALUE");
        for (String _if: ifString.split("\n")){
            patch += _if + "{" + fixString + "}\n";
        }
        return patch;
    }
}
