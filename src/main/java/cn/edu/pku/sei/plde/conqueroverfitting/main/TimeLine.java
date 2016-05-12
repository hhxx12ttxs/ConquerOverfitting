package cn.edu.pku.sei.plde.conqueroverfitting.main;

/**
 * Created by yanrunfa on 5/4/16.
 */
public class TimeLine {
    private long startTime;
    private long timeLimit;
    private boolean timeoutNow = false;

    public TimeLine(int timeLimit){
        this.timeLimit = timeLimit;
        startTime = System.currentTimeMillis();
    }

    public boolean isTimeout(){
        if (timeoutNow){
            return true;
        }
        return (System.currentTimeMillis() - startTime)/1000 > timeLimit;
    }

    public void timeOutNow(){
        timeoutNow = true;
    }
}
