package uk.ac.rhul.cs.dice.golem.conbine.agent.anac.ValueModelAgent;



//import negotiator.Bid;
//import negotiator.issue.Value;
//import negotiator.utility.UtilitySpace;

public class ValueModeler{ 
	public boolean initialized=false;
//	UtilitySpace utilitySpace;
	IssuesDecreases[] issues;
	//initializing the opponenent model with the opponentFirstBid,
	//which is assumed to be the best bid possible
	public void initialize(double firstBid, int inintialPrice, int reservationPrice) throws Exception{
		initialized=true;
//		utilitySpace  = space;
		int issueCount = 1;
		issues = new IssuesDecreases[issueCount];
		for(int i =0; i<issueCount;i++) {
			double value = firstBid;
			issues[i] = new IssuesDecreases(inintialPrice, reservationPrice);
			//System.out.printf("%d issue init\n", i);
			issues[i].initilize(inintialPrice, reservationPrice);
			//System.out.printf("%d issue init succeeded\n", i);
		}
	}
//	private void normalize(){
//		double sumWeight =0.0001;
//		for(int i=0;i<issues.length;i++){
//			sumWeight += issues[i].weight();
//		}
//		for(int i=0;i<issues.length;i++){
//			//issues[i].normalize(issues[i].weight()/sumWeight);
//			//OK so it dosn't really sum up to 1...
//			issues[i].normalize(issues[i].weight());
//		}
//		
//		
//	}
	//this function gets a reliability measurement of a value and determines
	//how much units of deviation should this reliability level move
	//for each movement of 100% reliability
	private double reliabilityToDevUnits(double reliability){
		if(reliability>1/2){
			return (1/(reliability*reliability));
		}
		if(reliability>1/4){
			return (2/reliability);
		}
		if(reliability>0){
			return (4/Math.sqrt(reliability));
		}
		//this case shouldn't be reached
		return 1000;
	}
	//the bid utility for the player is assumed to be 1-expectedDecrease
	public void assumeBidWorth(double bid,double expectedDecrease,double stdDev) throws Exception{
		ValueDecrease[] values = new ValueDecrease[1];
		double maxReliableDecrease=0;
//		for(int i=0;i<issues.length;i++){
//			Value value = bid.getValue(utilitySpace.getIssue(i).getNumber());
		values[0] = issues[0].getExpectedDecrease(bid);
//		}
		//double deviationUnit=stdDev;
		double deviationUnit=0;
//		for(int i=0;i<issues.length;i++){
			deviationUnit +=
				reliabilityToDevUnits(values[0].getReliabilty())
				*values[0].getDeviance();
			if(maxReliableDecrease<values[0].getDecrease() && values[0].getReliabilty()>0.8){
				maxReliableDecrease=values[0].getDecrease();
//			}
		}
		ValueDecrease origEvaluation=utilityLoss(bid);
		double unitsToMove = (expectedDecrease-origEvaluation.getDecrease())/deviationUnit;
		//System.out.printf("originaly %f with %f reliability, want %f, moving %f steps\n", origEvaluation.getDecrease(),origEvaluation.getReliabilty(),expectedDecrease,unitsToMove);
//		for(int i=0;i<issues.length;i++){
			//System.out.printf("issue %d value %s original dec %f,rel %f,dev %f ",i,bid.getValue(utilitySpace.getIssue(i).getNumber()).toString(),values[i].getDecrease(),values[i].getReliabilty(),values[i].getDeviance());
			double newVal = values[0].getDecrease()+
				reliabilityToDevUnits(values[0].getReliabilty())
				*values[0].getDeviance()
				*unitsToMove;
			if(values[0].getMaxReliabilty()>0.7 || maxReliableDecrease<newVal){
				if(newVal>0){
					values[0].updateWithNewValue(newVal, origEvaluation.getReliabilty());
				}
				else values[0].updateWithNewValue(0, origEvaluation.getReliabilty());
			}
			//assumes that new unreliable values costs more than previously seen values.
			//if our opponent selected a bid that costs 10%,
			//that is split between values that costs 4%,6%.
			//than if 4%->6% we will think that 6%->4%.
			//worst this sway also influences the estimate
			//of our opponent's concession, so we may think he
			//Consented to 7% and 6%->1%. 
			//both issues require this failsafe...
			//else values[i].updateWithNewValue(maxReliableDecrease, origEvaluation.getReliabilty());
			else values[0].updateWithNewValue(newVal, origEvaluation.getReliabilty());
			
			//System.out.printf("current value dec %f,rel %f,dev %f\n",values[i].getDecrease(),values[i].getReliabilty(),values[i].getDeviance());
//		}
//		normalize();
		
	}
	public ValueDecrease utilityLoss(double bid) throws Exception{
		ValueDecrease[] values = new ValueDecrease[issues.length];
//		for(int i=0;i<issues.length;i++){
//			Value value = bid.getValue(utilitySpace.getIssue(i).getNumber());
			values[0] = issues[0].getExpectedDecrease(bid);
//		}
		double stdDev = 0;
//		for(int i=0;i<issues.length;i++){
			stdDev += values[0].getDeviance();
//		}
		double decrease=0;
//		for(int i=0;i<issues.length;i++){
			decrease += values[0].getDecrease();
//		}
		//the sum square of 1/reliability
		double sumSQ=0;
//		for(int i=0;i<issues.length;i++)
		{
			double rel = values[0].getReliabilty();
			rel = rel>0?rel:0.01;
			sumSQ += (1/rel)*(1/rel);
		}
		//added postBG 
		sumSQ/=issues.length;
		double rel = Math.sqrt(1/sumSQ);
		return new ValueDecrease(decrease,rel,stdDev);
	}
	
	public IssuesDecreases getIssue(int index){
		if(index<issues.length && index>=0){
			return issues[index];
		}
		return issues[0];
		
	}
	
}

