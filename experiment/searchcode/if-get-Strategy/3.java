public void setStrategy(Strategy strategy) {
this.strategy = strategy;
}
public double getPersonScore(double[] a){
if(this.strategy==null){
return 0;
}else{
return strategy.computeScore(a);
}
}
}

