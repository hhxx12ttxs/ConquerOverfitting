package roulette;

public class TestTimer implements Timer {
long currentMillis;
long executeRunnableMillis;
Runnable runnableToExecute;
public void callBack(long howMuchLaterInMillis, Runnable what){

