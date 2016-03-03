package cn.edu.pku.sei.plde.conqueroverfitting.trace;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yanrunfa on 16/2/21.
 */
public class TraceResult implements Serializable {
    private Map<String, List<String>> result = new HashMap<String, List<String>>();
    private final boolean _testResult;

    /**
     *
     * @param testResult if the test is success or fail
     */
    public TraceResult(boolean testResult){
        _testResult = testResult;
    }

    public boolean getTestResult(){
        return _testResult;
    }

    public Map<String, List<String>> getResultMap(){
        return result;
    }

    /**
     *
     * @param key the key of map
     * @param value the value of map
     */
    public void put(String key, String value){
        List<String> values = result.get(key);
        if (values == null){
            values = new ArrayList<String>();
        }
        if (!values.contains(value)){
            values.add(value);
            result.put(key, values);
        }
    }

    /**
     *
     * @param key get value from key
     * @return the value
     */
    public List<String> get(String key){
        return result.get(key);
    }
}
