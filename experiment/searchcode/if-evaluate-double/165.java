package com.lqyandpy.crf;

public class SignFunction implements ActivationFunction{
	@Override
	public double evaluate(double argD) {
		// TODO Auto-generated method stub
		if(argD<-45){
			return 0;
		}else if(argD>45){
			return 1;
		}
			
		return 1/(1+Math.pow(Math.E, (-1d)*argD));
	}

	@Override
	public double derivation(double argD) {
		// TODO Auto-generated method stub
		return (1-this.evaluate(argD))*this.evaluate(argD);
	}
	
	
}

