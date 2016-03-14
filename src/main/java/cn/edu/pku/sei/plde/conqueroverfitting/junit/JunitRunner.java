package cn.edu.pku.sei.plde.conqueroverfitting.junit;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

/**
 * Created by yanrunfa on 16/3/13.
 */
public class JunitRunner {
    public static void main(String... args) throws ClassNotFoundException {
        String[] classAndMethod = args[0].split("#");
        Request request = Request.method(Class.forName(classAndMethod[0]), classAndMethod[1]);
        Result result = new JUnitCore().run(request);
        if (!result.wasSuccessful()){
            System.out.println("E");
        }
        System.out.println("<<");
        System.exit(result.wasSuccessful() ? 0 : 1);
    }
}
