package cn.edu.pku.sei.plde.conqueroverfitting.main;

/**
 * Created by yanrunfa on 5/4/16.
 */
public class TimeLine {
    private long startTime;
    private long timeLimit;

    public TimeLine(int timeLimit){
        this.timeLimit = timeLimit;
        startTime = System.currentTimeMillis();
    }

    public boolean isTimeout(){
        return (System.currentTimeMillis() - startTime)/1000 > timeLimit;
    }
}
