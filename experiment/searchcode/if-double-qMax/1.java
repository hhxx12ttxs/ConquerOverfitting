package tools.DistanceMatrix.Transform;

import tools.DistanceMatrix.DistanceObject;

public class embossScoreToDistance implements Transform {

	private double tmax,qmax;
	
	public embossScoreToDistance(){
	}
	
	public double getQmax() {
		return qmax;
	}

	public void setQmax(double qmax) {
		this.qmax = qmax;
	}

	public double getTmax() {
		return tmax;
	}

	public void setTmax(double tmax) {
		this.tmax = tmax;
	}

	public DistanceObject transform(DistanceObject d) {
		double dist=1;
		if(d.getDistance()>=0){
			dist=1-2*(4);
		}
		return new DistanceObject(d.getQname(),d.getTname(),dist);
	}

}

