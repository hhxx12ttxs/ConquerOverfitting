package cn.edu.pku.sei.plde.conqueroverfitting.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.rmi.server.UID;
import java.security.ProtectionDomain;
import java.util.Arrays;

/**
 * Created by yanrunfa on 16/3/9.
 */
public class CommentAssertTransformer implements ClassFileTransformer {
    public String _testSrcPath;
    public String _testClassName;

    public CommentAssertTransformer(String testSrcPath, String testClassName){
        _testSrcPath = testSrcPath;
        _testClassName = testClassName.trim().replace(".","/");
        System.out.println("CommentAssertTransformer Start");
    }



    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        /* handing the anonymity class */
        if (className.contains(_testClassName.substring(_testClassName.lastIndexOf("/"))) && className.contains("$")){
            String tempAnonymityClassName = _testSrcPath.substring(0,_testSrcPath.lastIndexOf("/")-1)+className.substring(className.lastIndexOf("/")+1)+".class";
            if (!new File(tempAnonymityClassName).exists()){
                return classfileBuffer;
            }
            byte[] result = Utils.getBytesFromFile(tempAnonymityClassName);
            if (result == null){
                return classfileBuffer;
            }
            return result;
        }
        /* skip the other classes*/
        if (!className.equals(_testClassName)) {
            return classfileBuffer;
        }
        else {
            if (!new File(_testSrcPath.trim()).exists()){
                return classfileBuffer;
            }
            return Utils.getBytesFromFile(_testSrcPath.trim());
        }
    }





}
