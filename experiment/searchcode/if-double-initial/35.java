private MyLMA cf = null;
private double[] initialParams = null;
public LineFitter(double[] initialGuess,double[] xx,double[] yy){
cf = new MyLMA(this,initialGuess,data);
}

public LineFitter(double[] initialGuess,Vector2D[] v){

