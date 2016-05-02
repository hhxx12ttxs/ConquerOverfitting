package uk.ac.rhul.cs.dice.golem.conbine.agent.anac;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.naming.NoInitialContextException;

import Jama.Matrix;

import sun.reflect.generics.tree.Tree;
import uk.ac.rhul.cs.dice.golem.action.Action;
import uk.ac.rhul.cs.dice.golem.agent.AgentBrain;
import uk.ac.rhul.cs.dice.golem.conbine.action.NegotiationAction;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AbstractSellerAgent;
import uk.ac.rhul.cs.dice.golem.conbine.agent.AgentParameters;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DefaultDialogueStateSeller;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueState;
import uk.ac.rhul.cs.dice.golem.conbine.agent.DialogueStateSeller;
import uk.ac.rhul.cs.dice.golem.util.Logger;


//import negotiator.Agent;
//import negotiator.AgentID;
//import negotiator.Bid;
//import negotiator.BidIterator;
//import negotiator.Domain;
//import negotiator.Timeline;
//import negotiator.actions.Accept;
//import negotiator.actions.Action;
//import negotiator.actions.Offer;
//import negotiator.issue.*;
//import negotiator.utility.UtilitySpace;

// The agent is similar to a bully.
// on the first few bids it steadily goes down to 0.9 of utility to make sure that if the opponent is trying to 
// profile me, he could do it more easily.
// after that, it goes  totally selfish and almost giving up no points.
// more over, the nicer the opponent is (Noise serves as a "niceness" estimation) the more selfish this agent gets.
// only at the last few seconds the agent panics and gives up his utility.
// I believe this is an excellent strategy, but sadly we did not have enough manpower and time
// to calibrate it and make it shine :(
// an opponent-model would have helped us improving the result on panic stages, and the noise(niceness) calculation is too rough.

//interface GahbonValueType
//{
//	void INIT ();
//	void UpdateImportance(double OpponentBid /* value of this Issue as received from opponent*/ );	
//	double GetNormalizedVariance ();
//	
//	int    GetUtilitiesCount (); // 
//	
//	double GetExpectedUtilityByValue (double V);
//}

public class Gahboninho extends  AbstractSellerAgent {
	public Gahboninho(AgentBrain brain,
			AgentParameters params, String product) {
		super(brain, params, product);	
		 setLogLevel(OFF);
	}	

	private class OpponnentModel
	{
		//UtilitySpace US;

		/**
		 *  provides prediction of how important each dispute element is, considering opponent's 
		 *  Behavior and "obsessions"
		 */
		
		// alternative ideas for calculating utility:
		// using k-nearest neigough, since opponent may use the same bid many times
		public class IssuePrediction // represents one Issue of the bid (e.g. Screen-Size/Brand)
		{
			// the following tells how preferable each option is
			
			class NumericValues
			{
				private final int DiscretizationResolution = 21;
				
//				private double TranslateValue (Value V)
//				{
//					if (V instanceof ValueInteger)
//						return ((ValueInteger)V).getValue();
//					
//					return ((ValueReal)V).getValue();
//				}
				
				int [] ValueFrequencies;
				
				private int GetFrequencyIndex (double V)
				{
					return (int)((V - MinValue) / 
							(MaxValue / (DiscretizationResolution-1)));
				}
				
				double MinValue;
				double MaxValue;
				
				boolean isFirstValue = true;
				double FirstValue; // we assume that if FirstValue == MaxValue, then MaxValue has max utility
					
				double BidFrequencyIndexSum = 0;
				double NormalizedVariance; // 0 to 1
				int OpponentBidCountToConsider = 0;
				

				// this method learns from opponent's choices
				public void UpdateImportance (double opponentBid /* value of this Issue as received from opponent*/ )
				{
					++OpponentBidCountToConsider;
					double BidValue = opponentBid;
					if (isFirstValue)
					{
						isFirstValue = false;
						
						// choose if the highest utility for the opponent is max-value or min-value
						if ( (MaxValue - BidValue) >= (BidValue - MinValue) )
							FirstValue = MaxValue;
						else
							FirstValue = MinValue;
					}
					
					++ValueFrequencies[GetFrequencyIndex(BidValue)];
					BidFrequencyIndexSum += GetFrequencyIndex(BidValue);
					
					double AverageFrequencyIndex = BidFrequencyIndexSum / OpponentBidCountToConsider;
					
					NormalizedVariance = 0;
					for (int i = 0; i < DiscretizationResolution; ++i)
					{
						double Distance = (AverageFrequencyIndex - i ) / (DiscretizationResolution-1);
						NormalizedVariance += (double)(this.ValueFrequencies[i]) * Distance * Distance;
					}
					
					//NormalizedVariance /= BidFrequencyIndexSum;
				}

				public double GetNormalizedVariance() 
				{	
					return NormalizedVariance;
				}
			
				public int GetUtilitiesCount() 
				{
					return this.DiscretizationResolution;
				}
				
				public double GetExpectedUtilityByValue (double V)
				{
					return Math.min(1, Math.max(0, 1 - Math.abs(V - FirstValue)));
				}

			
				public void INIT() 
				{
//					ValueFrequencies = new int[this.DiscretizationResolution];
//					if (I.getType() == ISSUETYPE.INTEGER)
//					{	
//						IssueInteger II = (IssueInteger)I;
//						MaxValue = II.getUpperBound();
//						MinValue = II.getLowerBound();
//					}
//					else
//					{
//						IssueReal RI = (IssueReal)I;
//						MaxValue = RI.getUpperBound();
//						MinValue = RI.getLowerBound();
//					}
					MaxValue = getReservationPrice();
					MinValue = getInitialPrice();
					
				}

			}
//			class DiscreteValues implements GahbonValueType
//			{
//				TreeMap<ValueDiscrete,Integer>  OptionIndexByValue = null;
//				TreeMap<Integer,ValueDiscrete>  ValueByOptionIndex= null;			
//				
//				int								MostImportantValueOccurrencesAndBonuses = 1;
//
//				// TODO: make this funtion better! OptionOccurrencesCountWithoutSundayPreference's bonus
//				// should not be constant
//				int TotalImportanceSum = 0; // sum of all importances
//				private double						GetOptionImportance (int OptionIndex)
//				{
//					int FirstOptionBonus = UpdatesCount / ( OptionOccurrencesCountByIndex.length); // the first choice always gets a large bonus, so this may minimize "noises
//					
//					if (FirstIterationOptionIndex == OptionIndex)
//					{
//						return OptionOccurrencesCountByIndex [OptionIndex] + FirstOptionBonus;
//					}
//						                                               
//					return OptionOccurrencesCountByIndex [OptionIndex]; 
//						
//						
//				}
//				
//				int 							UpdatesCount = 0;
//				int 				   	    	FirstIterationOptionIndex = -1; // First Iteration has much more weight than other iterations, since it is reasonable to assume it indicates opponent's optimal option
//				int[] 			   	    		OptionOccurrencesCountByIndex = null; //Tells how many times each option was chosen by opponent
//				TreeMap<Integer,Integer> 		OptionIndexByImportance = new TreeMap<Integer, Integer>(); 
//				double  				    	IssueImportanceRankVariance = 0; // normalized ( 0 to 1, where 1 is maximum variance)
//				
//				// this method learns from opponent's choices
//				public void UpdateImportance (Value OpponentBid /* value of this Issue as received from opponent*/ )
//				{
//					++UpdatesCount;
//					
//					Integer incommingOptionIndex = OptionIndexByValue.get(OpponentBid);
//					if ( -1 == FirstIterationOptionIndex )
//						FirstIterationOptionIndex = incommingOptionIndex;
//					++OptionOccurrencesCountByIndex [incommingOptionIndex];
//										
//					// let OptionIndexByOccurrencesCount sort the options by their importance rank:
//					OptionIndexByImportance.clear();
//					
//					MostImportantValueOccurrencesAndBonuses = 0;
//					TotalImportanceSum = 0;
//					for (int OptionIndex = 0; OptionIndex < OptionOccurrencesCountByIndex.length; ++OptionIndex)
//					{	
//						int OptionImportance = (int)GetOptionImportance (OptionIndex);
//						MostImportantValueOccurrencesAndBonuses = Math.max(MostImportantValueOccurrencesAndBonuses, OptionImportance);
//						
//						OptionIndexByImportance.put(OptionImportance , OptionIndex); 
//						TotalImportanceSum += OptionImportance;
//						
//					}
//					
//					// now calculate how easily the opponent gives up his better options in this issue:
//					double AverageImportanceRank = 0; 
//					int currentOptionRank = 0; // highest rank is 0
//					for (Integer currentOptionIndex : OptionIndexByImportance.values())
//					{
//						AverageImportanceRank += currentOptionRank * GetOptionImportance(currentOptionIndex);
//						++currentOptionRank;
//					}
//					AverageImportanceRank /= TotalImportanceSum;
//					
//					IssueImportanceRankVariance = 0;
//					currentOptionRank = 0; // highest rank is 0
//					for (Integer currentOptionIndex : OptionIndexByImportance.values())
//					{
//						double CurrentOptionDistance =  (AverageImportanceRank - currentOptionRank) / 
//							OptionOccurrencesCountByIndex.length; // divide by option count to normalized distances
//						  
//						IssueImportanceRankVariance +=  OptionOccurrencesCountByIndex[currentOptionIndex] * // Occurrence count of current option
//							(CurrentOptionDistance * CurrentOptionDistance); // variance of current option
//						
//						++currentOptionRank;
//					}
//					
//					IssueImportanceRankVariance /= TotalImportanceSum;
//				}
//
//				public double GetNormalizedVariance() 
//				{	
//					return IssueImportanceRankVariance;
//				}
//
//			
//				public int GetUtilitiesCount() 
//				{
//					return ValueByOptionIndex.size();
//				}
//
//				public double GetExpectedUtilityByValue (Value V)
//				{
//					int ValueIndex = (Integer)(OptionIndexByValue.get(V));
//					return GetOptionImportance (ValueIndex) / MostImportantValueOccurrencesAndBonuses;
//				}
//				public void INIT(negotiator.issue.Issue I) 
//				{
//					IssueDiscrete DI = (IssueDiscrete)I;
//					OptionOccurrencesCountByIndex = new int[DI.getNumberOfValues()];
//					
//					Comparator<ValueDiscrete> DIComparer = new Comparator<ValueDiscrete>() 
//					{ 
//						public int compare(ValueDiscrete o1, ValueDiscrete o2) 
//						{return o1.value.compareTo(o2.value);}
//					};
//					OptionIndexByValue = new TreeMap<ValueDiscrete, Integer>(DIComparer);
//					ValueByOptionIndex = new TreeMap<Integer, ValueDiscrete> ();
//					
//					for (int ValueIndex = 0; ValueIndex < DI.getNumberOfValues(); ++ValueIndex)
//					{
//						OptionOccurrencesCountByIndex [ValueIndex]= 0;
//						
//						ValueDiscrete V = DI.getValues().get(ValueIndex);
//						OptionIndexByValue.put(V, ValueIndex);
//					}
//					
//				}
//
//			
//				
//				
//			}
			
			//public double						   ExpectedWeight; // depends on comparison of this issue's variance and other issues'
			//public GahbonValueType 				   Issue;   
			//public negotiator.issue.Issue		   IssueBase; 				

			public void INIT ()
			{
				// check what type of issue we are talking about
//				if ( I instanceof IssueDiscrete ) 
//				{
//					IssueDiscrete DI = (IssueDiscrete)I;
//					String[] values = new String[DI.getValues().size()];
//					int ValueIndex = 0;
//					for (ValueDiscrete v : DI.getValues())
//						values [ValueIndex++] = new String(v.value);
//					IssueBase = new IssueDiscrete(DI.getName(),DI.getNumber(),values);
//					Issue = new DiscreteValues();
//					Issue.INIT(I);
//				}
//				else if (I instanceof IssueReal)
//				{
//					IssueReal RI = (IssueReal)I;
//					IssueBase = new IssueReal(RI.getName(),RI.getNumber(),RI.getLowerBound(), RI.getUpperBound());
					NumericValues Issue = new NumericValues();
					Issue.INIT();
//				}
//				else if (I instanceof IssueInteger)
//				{
//					IssueInteger II = (IssueInteger)I;
//					IssueBase = new IssueReal(II.getName(),II.getNumber(),II.getLowerBound(), II.getUpperBound());
//					Issue = new NumericValues();
//					Issue.INIT(I);
//				}
//				
//				
//			}

		}
		public IssuePrediction[] IssuesByIndex = null; // holds all Issues, by index corresponding to Domain's
		//public TreeMap<Integer,Integer> IPIndexByIssueNumber = new TreeMap<Integer, Integer>();
		public double TotalIssueOptionsVariance;
				
		public void OpponnentModel () 
		{
			// utilitySpace is derived (and initialized) from Agent
			
			//this.US = utilitySpace;
			
			//IssuesByIndex = new IssuePrediction[US.getDomain().getIssues().size()];
			//ArrayList<Issue> IA = US.getDomain().getIssues();
			
			
			//for (int IssueIndex =0; IssueIndex < IA.size() ; ++IssueIndex)
			//{
				IssuesByIndex [0] = new IssuePrediction();
				//IssuesByIndex [0].INIT(IA.get(IssueIndex)); 
				//IPIndexByIssueNumber.put(IA.get(IssueIndex).getNumber(), IssueIndex);
			//}
		}
		
		TreeMap<String, Double> OpponentBids = new TreeMap<String, Double>(); 
		
		
		//Bedour: its only for issue weights
//		public void UpdateImportance (double OpponentBid /* Incomming Bid as received from opponent*/ ) throws Exception
//		{
//			String bidStr = OpponentBid.toString();
//			if ( OpponentBids.containsKey(bidStr) )
//				return;
//			OpponentBids.put(bidStr, OpponentBid);
//			
//				
//			final double ZeroVarianceToMinimalVarianceWeight = 3; 
//				// a heuristic value that tells us that an issue with no variance
//			    // is 3 times as important as the issue with minimal variance
//			
//			TotalIssueOptionsVariance = 0;
//			
//			double MinimalNonZeroVariance = Double.MAX_VALUE;
//			int ZeroVarianceIssueCount = 0; // 
//			for (int IssueIndex = 0; IssueIndex < IssuesByIndex.length; ++IssueIndex)
//			{
//				int IssueID = IssuesByIndex[IssueIndex].IssueBase.getNumber();
//				IssuesByIndex [IssueIndex].Issue.UpdateImportance(OpponentBid.getValue(IssueID));
//
//				double IssueVariance = IssuesByIndex [IssueIndex].Issue.GetNormalizedVariance();
//				
//				TotalIssueOptionsVariance += IssueVariance;
//				
//				if (0 == IssueVariance )
//					++ZeroVarianceIssueCount;
//				else
//					MinimalNonZeroVariance = Math.min(IssueVariance, MinimalNonZeroVariance);
//			}
//			
//			// we decide how important each issue is, by comparing it's variance
//			// to the most important issue (with minimal variance)
//			
//			TotalIssueOptionsVariance /= MinimalNonZeroVariance * (1.0 / ZeroVarianceToMinimalVarianceWeight); // we now count importance of issue with units 
//			// of size (1.0 / ZeroVarianceToMinimalVarianceWeight) * MinimalNonZeroVariance 
//			
//			// we add one unit per Zero-Variance Issue
//			TotalIssueOptionsVariance +=  ZeroVarianceIssueCount;
//			
//			double WeightCount = 0;
//			
//			
//			if (TotalIssueOptionsVariance != ZeroVarianceIssueCount) // check if all weights are not the same ( all variances zero)
//			{
//				// zero variance issue have exactly 1 VarianceUnits,
//				// next minimal variance had ZeroVarianceToMinimalVarianceWeight VarianceUnits
//				// other issues are weighted with same relation
//				double VarianceUnit = MinimalNonZeroVariance/ZeroVarianceToMinimalVarianceWeight;
//				
//				// calculate each issue weight (each weight is 0 to 1)
//				for (int IssueIndex = IssuesByIndex.length -1; IssueIndex >= 0; --IssueIndex)
//				{
//					if (0 == IssuesByIndex [IssueIndex].Issue.GetNormalizedVariance())
//					{
//							// if the issue has 0 variance, we give it maximum weight 
//							// more weight than the next (non-zero variance) important issue
//							IssuesByIndex [IssueIndex].ExpectedWeight = 1; 
//					}
//					else
//						IssuesByIndex [IssueIndex].ExpectedWeight = VarianceUnit / IssuesByIndex [IssueIndex].Issue.GetNormalizedVariance(); 
//												
//					WeightCount += IssuesByIndex [IssueIndex].ExpectedWeight;
//				}
//			} 
//			
//			for (int IssueIndex = IssuesByIndex.length -1; IssueIndex >= 0; --IssueIndex)
//			{
//				// if up until now we were always given the same bid, then all issues has the same importance and same variance(0)
//				if ( TotalIssueOptionsVariance == ZeroVarianceIssueCount)
//					IssuesByIndex [IssueIndex].ExpectedWeight = 1.0 / IssuesByIndex.length;
//				else
//					IssuesByIndex [IssueIndex].ExpectedWeight /= WeightCount; // normalize weights  
//			}	
//		}
//		public double EvaluateOpponentUtility (double B) throws Exception
//		{
//			double UtilitySum = 0;
//			
//			try
//			{	
//				for (int IssueIndex = 0; IssueIndex < IssuesByIndex.length; ++IssueIndex)
//				{
//					int IssueID = IssuesByIndex[IssueIndex].IssueBase.getNumber();
//					UtilitySum += IssuesByIndex[IssueIndex].ExpectedWeight * 
//						IssuesByIndex[IssueIndex].Issue.GetExpectedUtilityByValue( B.getValue(IssueID) );
//				}
//			}
//			catch (Exception e)
//			{
//				e.printStackTrace();
//			}
//			return UtilitySum;
//		}
//		TreeMap< Integer,TreeMap <String, ValueDiscrete> > ValueTranslation = new TreeMap<Integer, TreeMap<String,ValueDiscrete>>();
		//Bedour: for descrite issues
//		public Value ImproveValue (int IssueNumber, ValueDiscrete ValToImprove) throws Exception
//		{
//			TreeMap <String, ValueDiscrete> ValueTranslator  = ValueTranslation.get(IssueNumber);
//			ValueDiscrete resultValue = new ValueDiscrete(ValToImprove.toString()); 
//			
//			if ( ValueTranslator != null)
//			{
//				resultValue = ValueTranslator.get(ValToImprove.toString());
//				if (resultValue != null)
//					return resultValue;	
//			}
//			else
//			{
//				ValueTranslator = new TreeMap <String, ValueDiscrete> ();
//				ValueTranslation.put(IssueNumber, ValueTranslator);
//			}
//			
//			IssuePrediction IS = IssuesByIndex[IPIndexByIssueNumber.get(IssueNumber)];
//			Issue I = IS.IssueBase;
//			Bid tmpBid =  utilitySpace.getDomain().getRandomBid();
//			tmpBid.setValue(IssueNumber, ValToImprove);
//			
//			double oppUtilityWithVal = IS.Issue.GetExpectedUtilityByValue(ValToImprove);
//			double utilityWithVal = utilitySpace.getEvaluation(IssueNumber, tmpBid); 
//				
//			if (! ( I instanceof IssueDiscrete))
//				return ValToImprove;
//			
//			IssueDiscrete DI = (IssueDiscrete)I ;
//			
//				
//			int size = DI.getNumberOfValues();
//			for (int i=0;i<size;i++)
//			{
//				ValueDiscrete curr = DI.getValue(i);
//				tmpBid.setValue(IssueNumber, curr);
//				double myUtilityWithCurrent = utilitySpace.getEvaluation(IssueNumber, tmpBid);
//				double oppUtilityWithCurrent = IS.Issue.GetExpectedUtilityByValue(curr);
//				// // find a value which is not worse than valTo improve but better for opponent
//				if (myUtilityWithCurrent >= utilityWithVal &&
//					oppUtilityWithCurrent > oppUtilityWithVal * 1.3)
//				{
//					oppUtilityWithVal = oppUtilityWithCurrent;
//					resultValue = curr;
//				}
//			}
//			ValueTranslator.put(ValToImprove.toString(), resultValue);
//			
//			return resultValue;
//		}
//		public double ImproveBid (double BidToImprove) throws Exception
//		{
//			
//			double resultBid = generateNextOffer(dialogueId);
////			for (Issue issue : utilitySpace.getDomain().getIssues())
////			{
////				try
////				{
////					if (issue.getType() == ISSUETYPE.DISCRETE )
////						resultBid.setValue(issue.getNumber(), 
////										   ImproveValue(issue.getNumber(),
////												   		(ValueDiscrete) BidToImprove.getValue(issue.getNumber())));
////					else
////						resultBid.setValue(issue.getNumber(), 
////							   			   BidToImprove.getValue(issue.getNumber()));
////						
////				}
////				catch (Exception e)
////				{
////					try
////					{
////						resultBid.setValue(issue.getNumber(), 
////								(ValueDiscrete) BidToImprove.getValue(issue.getNumber()));
////					}
////					catch (Exception E)
////					{
////						return BidToImprove;
////					}
////				}
////				
////			}
//			
//			return resultBid=  BidToImprove ;
//		}
		public TreeMap<Double, Double> FilterBids (TreeMap<Double, Double> Bids, int DesiredResultEntries) throws Exception
		{
			TreeMap<Double, Double> resultBids = new TreeMap<Double, Double>();
			Entry<Double,Double> bidIter = Bids.lastEntry();
			
			double BestKey = bidIter.getKey();
			double bestBid = bidIter.getValue();
			double bestOppUtil = getUtility(bestBid);
			resultBids.put(BestKey, bestBid);
			
			bidIter = Bids.lowerEntry(bidIter.getKey());
			
			//int EntryContraction = Bids.size() / Math.max(1,DesiredResultEntries);
			//int RemainingEntriesToContraction = EntryContraction;
			
			Random rand = new Random();
			while (bidIter != null && getNormalisedTime(getStartTime()) < 0.94)
			{
				
				double checkedBid =  bidIter.getValue();
				double checkedKey = getUtility( checkedBid );
				double checkedOppUtil = getUtility(checkedBid);
				
				if ( checkedOppUtil >= bestOppUtil * 0.84)
				{
					resultBids.put(checkedKey, checkedBid);
					if ( checkedOppUtil > bestOppUtil )
					{
						bestBid = checkedBid;
						BestKey = checkedKey;
						bestOppUtil = checkedOppUtil;
					}
				}

				bidIter = Bids.lowerEntry(bidIter.getKey());
			}
			
			//if (bestBid != null)
			//	resultBids.put(BestKey, bestBid);
			
			if (resultBids.size() < DesiredResultEntries / 10 || resultBids.size() < 20)
				return Bids;
			return resultBids;
		}
	}
	
	}//end opponent model

	final int PlayerCount = 8; // if player count is 10, then we
	// may give 10 points to opponent in order to give 1 point to ourselves
	
	class IssueManager
	{
		String dialogueId;
		// world state :
		long 	 		 T;
		//UtilitySpace 		 US;
		int 				 TotalBiddingPossibilities; // amount of all possible bidsd
		// oponent properties :
		double				 OpponentBestBid; // the first bid opponent gave us
		double				 MyUtilityOnOpponentBestBid = 0;
		double 				 Noise = 0.4; // 0 to 1 - Tells us how varied the opponent's bids are, lately
		// My State:
		double 				 NextOfferUtility = 1;
		boolean 			 FirstOfferGiven = false;
		TreeMap<Double, Double> Bids;
		ArrayList<Double> relevantValuesPerIssue = new ArrayList<Double>();
		double maxBid = 0;
		
		boolean 			 InFrenzy = false;
		
//		public double GetMaxBidWithNoCost () throws Exception
//		{
//			double maxBid = generateNextOffer(dialogueId);
//			double justBidding = generateNextOffer(dialogueId);
//			
//			//for (Issue issue : this.US.getDomain().getIssues())
//			//{	
//				double tmpUtil;
//				double maxUtil = 0;
//				int maxUtilValIndex = 0;
//				
//			//	switch(issue.getType()) 
//				//{
//				
////				case INTEGER:
////					
////					IssueInteger integerIssue =(IssueInteger)issue;
////					
////					justBidding.setValue(issue.getNumber(), new ValueInteger(integerIssue.getUpperBound()));
////					maxUtil = US.getUtility(justBidding);
////					
////					justBidding.setValue(issue.getNumber(), new ValueInteger(integerIssue.getLowerBound()));
////					tmpUtil = US.getUtility(justBidding);
////					
////					if (maxUtil > tmpUtil)
////						maxBid.setValue(issue.getNumber(), new ValueInteger(integerIssue.getUpperBound()));
////					else
////						maxBid.setValue(issue.getNumber(), new ValueInteger(integerIssue.getLowerBound()));
////					
////					break;
//					
//				//case REAL: 
//					
//				//	IssueReal realIssue =(IssueReal)issue;
//					
//					
////					justBidding.setValue(issue.getNumber(), new ValueReal(realIssue.getUpperBound()));
////					maxUtil = UgetUtility(justBidding);
////					
////					justBidding.setValue(issue.getNumber(), new ValueReal(realIssue.getLowerBound()));
////					tmpUtil = US.getUtility(justBidding);
////					
////					if (maxUtil > tmpUtil)
////						maxBid.setValue(issue.getNumber(), new ValueReal(realIssue.getUpperBound()));
////					else
////						maxBid.setValue(issue.getNumber(), new ValueReal(realIssue.getLowerBound()));
//
////					break;
////				case DISCRETE:
////					IssueDiscrete discreteIssue = (IssueDiscrete)issue;
////					int size = discreteIssue.getNumberOfValues();
////					for (int i=0;i<size;i++)
////					{
////						justBidding.setValue(issue.getNumber(),discreteIssue.getValue(i) );
////						tmpUtil = US.getUtility(justBidding);
////						if (tmpUtil > maxUtil)
////						{
////							maxUtilValIndex = i;
////							maxUtil = tmpUtil;
////						}
////					}
////					
////					maxBid.setValue(issue.getNumber(), discreteIssue.getValue(maxUtilValIndex));
////					break;
////				}
////			}
////			
//			return maxBid;
//		}
		// fill utility-to-bid map here:
		public IssueManager (String dialogueId)
		{
			//this.T = T;
			this.dialogueId = dialogueId;
		    //try 
			//{
				//maxBid = GetMaxBidWithNoCost (); // try sparing the brute force
				maxBid = getInitialPrice();
				double maxBidUtil = getUtility(maxBid);
				//if ( maxBidUtil == 0) // in case cost comes into play
				//	this.maxBid = this.US.getMaxUtilityBid(); // use only if the simpler function won't work 
			
//			} 
//			catch (Exception e) 
//			{
//				try { this.maxBid = this.US.getMaxUtilityBid(); }
//				catch (Exception e2){}
//			}
			
			Bids  = new TreeMap<Double, Double>();
			
			//for (int i = 0; i < US.getDomain().getIssues().size(); ++i)
			//{
			//	Issue I = (Issue) US.getDomain().getIssue(i);
			//	if (I.getType() == ISSUETYPE.DISCRETE)
			//	{
			//		IssueDiscrete ID = (IssueDiscrete)I;
					
//					DifferentValuesCountPerIssueNum.put(ID.getNumber(), ID.getNumberOfValues() );
//					OutgoingValueAppeareancesByIssueNum.put(ID.getNumber(), new TreeMap<String, Integer>());
//				}
//				else if (I.getType() == ISSUETYPE.REAL)
//				{
//					DifferentValuesCountPerIssueNum.put(I.getNumber(), (int) DiscretisationSteps);
//					OutgoingValueAppeareancesByIssueNum.put(I.getNumber(), new TreeMap<String, Integer>());
//				}
//				else if (I.getType() == ISSUETYPE.INTEGER)
//				{
//					IssueInteger II = (IssueInteger)I;
//					DifferentValuesCountPerIssueNum.put(I.getNumber(), 
//							Math.min((int) DiscretisationSteps, 
//									  II.getUpperBound() - II.getLowerBound() + 1 ) );
//					
//					OutgoingValueAppeareancesByIssueNum.put(I.getNumber(), new TreeMap<String, Integer>());
//				}
//			}
//			ClearIncommingStatistics ();
		}
		void ClearIncommingStatistics ()
		{
			//for (Issue I : US.getDomain().getIssues())
			IncomingValueAppeareancesByIssueNum = new TreeMap<String,Integer>();
			
		}
		public double getMaxBid()
		{
			return maxBid;
		}
		private double GetDiscountFactor ()
		{
//			if (US.getDiscountFactor() <= 0.001 || US.getDiscountFactor() > 1)
//				return 1;
			return 1;
		}	
		//Random R = new Random();
		private void addPossibleValue(double offer)
		{
			if (!this.relevantValuesPerIssue.contains(offer))
			{
				this.relevantValuesPerIssue.add(offer);
			}
			
			
//			int randIndex = 0;
//			if ( this.relevantValuesPerIssue.get(issue).size() > 0)
//				randIndex = Math.abs(  R.nextInt() ) % this.relevantValuesPerIssue.get(issue).size();
//			this.relevantValuesPerIssue.get(issue).add(randIndex,val);
		}

		
		final double DiscretisationSteps = 20; // minimum 2
		private void buildIssueValues(double firstOppBid) throws Exception
		{
			
			double justBidding = firstOppBid;
//			
//			for (Issue issue : this.US.getDomain().getIssues())
//			{
				int AddedValues = 0;
//				
//				justBidding.setValue(issue.getNumber(), firstOppBid.getValue(issue.getNumber()));
				double utilityWithOpp= getUtility(justBidding);
//				
//				switch(issue.getType()) {
//				case INTEGER:
//					IssueInteger intIssue =(IssueInteger)issue;
//					
//					int iStep;
//					int totalSteps= (int) Math.min(DiscretisationSteps - 1, intIssue.getUpperBound() - intIssue.getLowerBound() );
//					iStep = Math.max(1, (int)((intIssue.getUpperBound() - intIssue.getLowerBound()) / totalSteps ));
//					
//					for (int i=intIssue.getLowerBound();i<=intIssue.getUpperBound();i+= iStep)
//					{
//						justBidding.setValue(issue.getNumber(), new ValueInteger(i));
//						double utilityWithCurrent = this.US.getEvaluation(issue.getNumber(), justBidding);
//						
//						// Only see it as a possible value if it is better for us than the opponent offer
//						if (utilityWithCurrent >= utilityWithOpp)
//						{
//							this.addPossibleValue(issue, new ValueInteger(i));
//						}
//					}
//					
//					AddedValues += Math.abs( intIssue.getUpperBound() - intIssue.getLowerBound() );
//					break;
//				case REAL: 
//					
//					IssueReal realIssue =(IssueReal)issue;
					double oneStep = (getInitialPrice()-getReservationPrice())/(DiscretisationSteps-1);
					for (double curr=getInitialPrice();curr<=getReservationPrice();curr-=oneStep)
					{
						justBidding=curr;
						double utilityWithCurrent = getUtility(justBidding);
						// Only see it as a possible value if it is better for us than the opponent offer
						if (utilityWithCurrent >= utilityWithOpp)
						{
							this.addPossibleValue(curr);
							AddedValues += 1000;
						}
					}

//					break;
//				case DISCRETE:
//					IssueDiscrete discreteIssue = (IssueDiscrete)issue;
//					int size = discreteIssue.getNumberOfValues();
//					for (int i=0;i<size;i++)
//					{
//						ValueDiscrete curr = discreteIssue.getValue(i);
//						justBidding.setValue(issue.getNumber(), curr);
//						double utilityWithCurrent = this.US.getEvaluation(issue.getNumber(), justBidding);
//						// Only see it as a possible value if it is better for us than the opponent offer
//						if (utilityWithCurrent >= utilityWithOpp)
//						{
//							this.addPossibleValue(issue, curr);
//							AddedValues += 1;
//						}
//
//					}
//					break;
//				}
				
				EffectiveDomainBids *= AddedValues;
			//}
		}

		
	
		Double BidsCreationTime = 0.0;
		double EffectiveDomainBids = 1;
		public void learnBids(double firstOppBid) throws Exception
		{
			this.buildIssueValues(firstOppBid);
			
						
			double startTime = getNormalisedTime(getStartTime());
			
			// very hard to deal with hash map, so copy it to arraylist:
//			Iterator<Entry<Issue,ArrayList<Value>>> MyIssueIterator = relevantValuesPerIssue.entrySet().iterator();
//			while (MyIssueIterator.hasNext())
//				IssueEntries.add(MyIssueIterator.next());
			
			// if there is a discount factor, don't take your time searching for bids
			BuildBid(0.05 * Math.pow( GetDiscountFactor(),0.6) + startTime);
			BidsCreationTime =  getNormalisedTime(getStartTime()) - startTime;
			
			// if there are about 5000 turns for the opponent, I expect him to be able to
			// give a good bid every few turns
			NoiseDecreaseRate = 0.01 * EffectiveDomainBids / ( 400 ); 
			NoiseDecreaseRate = Math.min(0.015, NoiseDecreaseRate); // beware of a too large rate 
			NoiseDecreaseRate = Math.max(0.003, NoiseDecreaseRate); 
			
		}
//		private ArrayList<Entry<Issue,ArrayList<Value>>> IssueEntries = new ArrayList<Entry<Issue,ArrayList<Value>>>();
		
		Random bidRand = new Random();
		int UtilityCollisionsLeft = 200000;
		private void BuildBid (double EndTime)
		{
			// TODO : build only bids with at least X util.
			// after some time, when we need lower utilities, re-build bid map, only this time consider
			// opponent's preferences: just build a method that tells us if one bid is preferrable (from opponent's point of view)
			// and if the less preferable one gives us less utility than the other bid, simply remove the bid from map
			
//			if ( getNormalisedTime(getStartTime()) < EndTime)
//			{	
//				if (EntrySetIndex < IssueEntries.size())
//				{
//					Entry<Issue, ArrayList<Value>> currentEntry = IssueEntries.get(EntrySetIndex);
//					for (Value v : currentEntry.getValue())
//					{
//						B.put(currentEntry.getKey().getNumber(), v);
//						BuildBid (B,EntrySetIndex + 1, EndTime);
//					}
//				}
//				else
//				{
//					try 
//					{
						
						double newBid = generateNextOffer(dialogueId);
						
						double BidUtil = getUtility(newBid);
						
						while ( UtilityCollisionsLeft > 0 && Bids.containsKey(BidUtil) )
						{
							--UtilityCollisionsLeft;
							BidUtil -= 0.002 / (bidRand.nextInt() % 9999999);
						}
						
						Bids.put( BidUtil , newBid );
						log("getUtility(newBid)"+getUtility(newBid));
						log(" : " + toString());
//					}
//					catch (Exception e) 
//					{
//						e.printStackTrace();
//					}	
//				}
				
//			}
		}
		
		public double CompromosingFactor = 0.95;
		// returns the minimum utility we want in the next offer we send
		
		double TimeDiffStart = -1;
		double TimeDiffEnd = -1;
		double RoundCountBeforePanic = 1; 
		
		double PrevRecommendation = 1;
		double MaximalUtilityDecreaseRate = 0.0009;
		public double 	GetNextRecommendedOfferUtility ()
		{		
			if (FirstOfferGiven == false)
			{
				FirstOfferGiven = true;
				return 1;
			}
			
			double Time =  getNormalisedTime(getStartTime());
			double Min = Math.pow(GetDiscountFactor (), 2 * Time);
			double Max = Min  * ( 1 + 6 * Noise * Math.pow(GetDiscountFactor (), 5) );
			
			if ( Time < 0.85 * GetDiscountFactor ())
				Min *= Math.max(BestEverOpponentBidUtil, 0.9125); 
			else if ( Time <= 0.92 * GetDiscountFactor ())
			{
				CompromosingFactor = 0.94;
				Min *= Math.max(BestEverOpponentBidUtil, 0.84); // never ever accept an offer with less than that utility
				
				Max /= Min; // slow down the decreasing
			
			}
			else if ( Time <= 0.94 * GetDiscountFactor ())
			{
				CompromosingFactor = 0.93;
				Min *= Math.max(BestEverOpponentBidUtil, 0.775);
				Max /= Min; 
			}
			else if ( Time <= 0.985 * Min &&
					  (BestEverOpponentBidUtil <= 2 * (1.0 / PlayerCount) ||// never accept an offer with less utility than that
					   Time <= (1 - 3 * (TimeDiffEnd - TimeDiffStart)/RoundCountBeforePanic ) ) )
			{
				CompromosingFactor = 0.91;
				MaximalUtilityDecreaseRate = 0.001;
				
				Min *= Math.max(BestEverOpponentBidUtil, 0.7);
				Max /= Min; 
				
				TimeDiffEnd = TimeDiffStart = Time;
			}
			else if (Time <= 0.9996 &&
					 (BestEverOpponentBidUtil <= 2 * (1.0 / PlayerCount) ||// never accept an offer with less utility than that
					  Time <= (1 - 3 * (TimeDiffEnd - TimeDiffStart)/RoundCountBeforePanic )) ) // until last few rounds
			{	
				TimeDiffEnd = Time;
				++RoundCountBeforePanic;
				
				MaximalUtilityDecreaseRate = 0.001 + 0.01 *  (Time - 0.985) / (0.015); 
				
				//MaximalUtilityDecreaseRate = 0.0018;
				//MaximalUtilityDecreaseRate = 0.0009;
				
				if (3 * (1.0 / PlayerCount) > BestEverOpponentBidUtil)
				{
					Min *= 3.5 * (1.0 / PlayerCount);
					CompromosingFactor = 0.8;
				}
				else
				{
					Min *= BestEverOpponentBidUtil;
					CompromosingFactor = 0.95;
				}
				
				Max /= Min;
			}
			else
			{
				log("aahhhh!");
				
				CompromosingFactor = 0.92;
				
				// as low as I can go!
				if (BestEverOpponentBidUtil < 2 * (1.0 / PlayerCount))
				{
					Min = 2 * (1.0 / PlayerCount); // 0.25 if 8 players
					Max = 1;
				}
				else
				{
					Max = Min = BestEverOpponentBidUtil;
					InFrenzy = true;
				}
				MaximalUtilityDecreaseRate = 1;
			}
			
			// the more eager the opponent is to settle, the slower we give up our utility. 
			// the higher the discount factor loss, the faster we give it up
			
			Max = Math.max(Max, Min);
			NextOfferUtility = Math.min(1, Max - (Max - Min) *  getNormalisedTime(getStartTime()));

			log("(");
			log(""+(int)(Noise * 100));
			log(")");
			log(""+(int)(Max * 100));
			log("	To	");
			log(""+(int)(Min*100));
			log("	In	");
			log(""+(double)((int)(getNormalisedTime(getStartTime()) * 10000))/ 10000);
			log("	Is	" );
			log(""+(double)((int)(NextOfferUtility * 10000))/ 10000 );
			
			// slow down the change:
			if (NextOfferUtility + MaximalUtilityDecreaseRate < PrevRecommendation )
				NextOfferUtility = PrevRecommendation - MaximalUtilityDecreaseRate;
			else if (NextOfferUtility - 0.005 > PrevRecommendation )
				NextOfferUtility = PrevRecommendation + 0.005;
				
			log("	-	" );
			log( ""+(double)((int)(NextOfferUtility * 10000))/ 10000 );
			
			PrevRecommendation = NextOfferUtility;
			return NextOfferUtility;
		}

		public double 	GetMinimumUtilityToAccept () // changes over time
		{
			return CompromosingFactor * GetNextRecommendedOfferUtility ();
		}
		
		int CountdownToNoiseReestimation;
		String EstimateValue (double x) throws Exception
		{
//			switch (I.getType())
//			{
//				case DISCRETE:
//					ValueDiscrete DV = (ValueDiscrete)v;
//					return DV.getValue();
//				case INTEGER:
//					int ValueIndex = 0;
//					IssueInteger II = (IssueInteger)I;
//					ValueInteger IV = (ValueInteger)v;
//					double Step = II.getUpperBound() - II.getLowerBound();
//					if (Step != 0)
//					{
//						int totalSteps= (int) Math.min(DiscretisationSteps, II.getUpperBound() - II.getLowerBound() + 1);
//						Step /= totalSteps;
//						ValueIndex = (int)(IV.getValue() / Step);
//					}
//					return String.valueOf(ValueIndex);
//				case REAL:
//					IssueReal RI = (IssueReal)I;
//					ValueReal RV = (ValueReal)v;
					double StepR =getInitialPrice() -getReservationPrice();
					int ValueIndex = 0;
					if (StepR != 0)
					{
						StepR /= DiscretisationSteps;
						ValueIndex = (int)(x / StepR);
					}
					return String.valueOf(ValueIndex);
//			}
//			
//			throw new Exception("illegal issue");
		}
	
		
		TreeMap<String/*value.ToString()*/,Integer>  IncomingValueAppeareancesByIssueNum = new TreeMap<String,Integer>(); // tells how many times each value has appeared in all incoming bids
		TreeMap<String/*value.ToString()*/,Integer>  OutgoingValueAppeareancesByIssueNum = new  TreeMap<String,Integer>(); // tells how many times each value has appeared in all incoming bids
		int					 						 	 DifferentValuesCountPerIssueNum=0 ; // tells how many different values are per Issue
		int 											  CountdownToStatisticsRefreshing = 20; // when hits zero, recalculate AverageDistance and reset Noise
		double 											  PreviousAverageDistance = 0.2; // 0 to 1
		double 											  PreviousCountdownOpponentBestBidUtil = 1;
		double 											  BestEverOpponentBidUtil = 0;
		double											  WorstOpponentBidEvaluatedOpponentUtil = 1;
		double											  PrevWorstOpponentBidEvaluatedOpponentUtil = 1;
		double											  PrevRoundWorstOpponentBidEvaluatedOpponentUtil = 1;
		double											  BestEverOpponentBid = 0;
		
		int 			OutgoingBidsCount = 0;		
		void 			AddMyBidToStatistics (double OutgoingBid) throws Exception
		{
			++OutgoingBidsCount;
			//for (Issue I : US.getDomain().getIssues())
		//	{
				String bidValueEstimation = EstimateValue(OutgoingBid);
				if ( OutgoingValueAppeareancesByIssueNum.containsKey(bidValueEstimation) )
				{
					OutgoingValueAppeareancesByIssueNum.put(bidValueEstimation, 
								OutgoingValueAppeareancesByIssueNum.get(bidValueEstimation) + 1);
				}
				else
				{
					OutgoingValueAppeareancesByIssueNum.put(bidValueEstimation, 1); 
				}
			}
			
			
			



		double NoiseDecreaseRate = 0.01;
		final int CountdownLength = 20;
		// update noise here
		public void ProcessOpponentBid (double IncomingBid, String id) throws Exception
		{
			//OpponnentModel  OM=  OMS.get(id);
			if (CountdownToStatisticsRefreshing > 0)
			{
				if (getUtility(IncomingBid) > BestEverOpponentBidUtil)
				{
					BestEverOpponentBidUtil = getUtility(IncomingBid);
					BestEverOpponentBid = IncomingBid;
					Bids.put(BestEverOpponentBidUtil, BestEverOpponentBid);
				}
				
				double getopUtil = getUtility(IncomingBid);
				
				if (PrevRoundWorstOpponentBidEvaluatedOpponentUtil < getopUtil)
					PrevRoundWorstOpponentBidEvaluatedOpponentUtil = getopUtil;
				if (WorstOpponentBidEvaluatedOpponentUtil > getopUtil)
				{
					WorstOpponentBidEvaluatedOpponentUtil = getopUtil;
					
				}
						
				--CountdownToStatisticsRefreshing;
				//for (Issue I : US.getDomain().getIssues())
			//	{

					String bidValueEstimation = EstimateValue(IncomingBid);
					if ( IncomingValueAppeareancesByIssueNum.containsKey(bidValueEstimation) )
					{
						IncomingValueAppeareancesByIssueNum.
							put(bidValueEstimation, 
								IncomingValueAppeareancesByIssueNum.get(bidValueEstimation) + 1);
					}
					else
					{
						IncomingValueAppeareancesByIssueNum.
							put(bidValueEstimation, 1);
					}
			//	}
			}
			else
			{
				double CurrentSimilarity = 0;
				
				//for (Issue I : US.getDomain().getIssues())
				//{
					for (String val : OutgoingValueAppeareancesByIssueNum.keySet() )
					{
						if (IncomingValueAppeareancesByIssueNum.containsKey(val))
						{
							float outgoingVal = ((float)(OutgoingValueAppeareancesByIssueNum.get(val))) / OutgoingBidsCount;
							float incomingVal = (((float)(IncomingValueAppeareancesByIssueNum.get(val))) / CountdownLength);
							float diff = outgoingVal - incomingVal;
							float diffSqr = diff * diff; // 0 to 1
								
							CurrentSimilarity += 
								(1.0 /1) * 
								(1.0 / DifferentValuesCountPerIssueNum ) * 
								(1 - diffSqr);
						}
					}
				//}
				
				
				if (CurrentSimilarity > PreviousAverageDistance )
				{
					Noise += 0.05; // opponent is trying harder to search
				}
				else if (BestEverOpponentBidUtil < PreviousCountdownOpponentBestBidUtil ||
						 WorstOpponentBidEvaluatedOpponentUtil < PrevWorstOpponentBidEvaluatedOpponentUtil)
				{
					Noise += NoiseDecreaseRate; // Apparently, the opponent just gave up some of his util
				}
				else 
				{
					Noise -= NoiseDecreaseRate;
					
					if (PrevRoundWorstOpponentBidEvaluatedOpponentUtil > WorstOpponentBidEvaluatedOpponentUtil * 1.2)
						Noise -= NoiseDecreaseRate;
					if (CurrentSimilarity * 1.1 < PreviousAverageDistance )
						Noise -= NoiseDecreaseRate;
				}
				//start their notes
//				if (CurrentSimilarity > PreviousAverageDistance ||
//					BestEverOpponentBidUtil > PreviousCountdownOpponentBestBidUtil)
//				{
//					Noise += 0.02;
//				}
//				else
//				{
//					Noise -= 0.02;
//				}
				
//				if (CurrentSimilarity > PreviousAverageDistance ||
//					BestEverOpponentBidUtil > PreviousCountdownOpponentBestBidUtil)
//				{
//					Noise += 0.02;
//				}
//				else if (CurrentSimilarity < PreviousAverageDistance &&
//						BestEverOpponentBidUtil < PreviousCountdownOpponentBestBidUtil)
//				{
//					Noise -= 0.06;
//				}
//				else
//					Noise -= 0.005;
//				end their notes
				

				Noise = Math.min ( Math.max(Noise, 0), 1);
				PreviousAverageDistance = CurrentSimilarity;
				CountdownToStatisticsRefreshing = CountdownLength;
				PreviousCountdownOpponentBestBidUtil = BestEverOpponentBidUtil;
				PrevRoundWorstOpponentBidEvaluatedOpponentUtil = 1;
				PrevWorstOpponentBidEvaluatedOpponentUtil = WorstOpponentBidEvaluatedOpponentUtil;
				ClearIncommingStatistics();
			}
		}
		
		public double GenerateBidWithAtleastUtilityOf (double MinUtility)
		{
			log("searching for :");
			log("MinUtility"+MinUtility);
			
			 Entry<Double,Double> e = Bids.ceilingEntry(MinUtility);
			 log("found : " );
			
			 if (e == null)
			 {
				 log(" not found :(  ");
				 try {
					log("maxBid utility:"+getUtility(maxBid));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					//e1.printStackTrace();
				}
				 return this.maxBid;
			 }
			
			 ////System.out.println(e.getKey());
			return e.getValue();
		}

}

	boolean WereBidsFiltered = false;
	private HashMap<String, Gahboninho.OpponnentModel> OMS= new HashMap<String, Gahboninho.OpponnentModel>();
	private HashMap<String, IssueManager> IMS = new HashMap<String, IssueManager>();
	//Gahboninho.OpponnentModel OM;
	//IssueManager IM;
	@Override
	
    protected DialogueState makeNewDialogueState(NegotiationAction offer) {
		
		DialogueStateANAC dialogueState = new DefaultDialogueStateANAC(
                offer.getDialogueId(),
                offer.getProtocol(),
                offer.getRecipient(),
                offer.getProductId());
		
   // 	DialogueState state=super.makeNewDialogueState(offer);
         log("debug : ----- Initialize -----");
         log("Run init for dialogue: " + offer.getDialogueId());
		// TODO Auto-generated method stub
		//super.init();
		
        IMS.put(offer.getDialogueId(), new IssueManager(offer.getDialogueId()));
        OMS.put(offer.getDialogueId(), new Gahboninho.OpponnentModel());
        //IM = new IssueManager(offer.getDialogueId());
		//OM = new OpponnentModel();
        IssueManager  IM=  IMS.get(offer.getDialogueId());
		IM.Noise *= IM.GetDiscountFactor ();
		
		return dialogueState;
	//	double previousBid = 0;
	//	double OpponentBid = 0;
	}

//	public String getName()
//	{
//		return "Gahboninho V3";
//	}
	
	
//	@Override
//	public void ReceiveMessage(Action opponentAction) 
//	{
//		this.previousBid = this.OpponentBid;
//		
//		if (opponentAction instanceof Offer)
//		{
//			OpponentBid = ((Offer)opponentAction).getBid();
//			if (this.previousBid != null)
//			{
//					try 
//					{
//						this.IM.ProcessOpponentBid(this.OpponentBid);
//						OM.UpdateImportance(OpponentBid);
//					} catch (Exception e) 
//					{
//						// Too bad
//					}
//			} 
//			else
//			{
//				try 
//				{
//					this.IM.learnBids(this.OpponentBid);
//					
//				} // learn from the first opp. bid
//				catch (Exception e) 
//				{
//					
//				}
//			}
//		}
//	}

	
	int RoundCount = 0;
	
	int FirstActions = 40;
	int TotalFirstActions = 40;
	
	
	
@Override
protected List<Action> decideActionBasedOnOffer(NegotiationAction offer) {
    	log("Recived: "+offer);
		List<Action> actionsToPerform = super.decideActionBasedOnOffer(offer);	
		DialogueStateANAC state = getDialogState(offer.getDialogueId());
		double opponentBid = Double.parseDouble(offer.getValue());
		double previousBid = state.getOpponentsLastBid();
		log("opponentBid: " + opponentBid);
		log("previousBid: " + previousBid);
		IssueManager  IM=  IMS.get(offer.getDialogueId());
		Gahboninho.OpponnentModel OM=  OMS.get(offer.getDialogueId());
		state.updateOpponentsLastBid(opponentBid);
		//this.previousBid = this.OpponentBid;
		
			//OpponentBid = ((Offer)opponentAction).getBid();
			if (previousBid != 0)
			{
					try 
					{
						IM.ProcessOpponentBid(opponentBid,offer.getDialogueId());
						//OM..IssuePrediction.NumericValues.UpdateImportance(opponentBid);
					} catch (Exception e) 
					{
						// Too bad
					}
			} 
			else
			{
				try 
				{
					IM.learnBids(opponentBid);
					
				} // learn from the first opp. bid
				catch (Exception e) 
				{
					
				}
			}
		

		if (state.getFirstRound())
		{
			state.updateFirstRound();
			//state.firstRound = !state.firstRound;
			log("Send initial price " + getInitialPrice());
			 actionsToPerform.addAll(super.sendCounterOffer(offer,getInitialPrice()));		
		}else{
		////System.out.println("\n\n");
		
			// on the first few rounds don't get tempted so fast
			if (FirstActions > 0 && opponentBid != 0 &&getUtility(opponentBid) > 0.95){
				actionsToPerform.addAll(super.acceptOpponentOffer(offer));
				log("I accept his offer of " + opponentBid);
			}
			else if (opponentBid != 0 && getUtility(opponentBid) >= IM.GetMinimumUtilityToAccept())
			{
				log("I accept his offer of " + opponentBid + "\nwith utility of :"+getUtility(opponentBid));
				actionsToPerform.addAll(super.acceptOpponentOffer(offer));
			}else{
			
			++RoundCount;
			if(WereBidsFiltered == false &&  
			   (getNormalisedTime(getStartTime()) >  IM.GetDiscountFactor () * 0.9 || 
					   getNormalisedTime(getStartTime()) + 3 * IM.BidsCreationTime > 1) ) /* we must filter to make last bids efficient*/
			{
				WereBidsFiltered = true;
				
				int DesiredBidcount = (int)(RoundCount * (1-getNormalisedTime(getStartTime())));
				
						
				log("Filtering...(	");
				log("DesiredBidcount"+DesiredBidcount);
				log("	)");
				//System.out.print(IM.Bids.size());
				//System.out.print("	to	");
				
				if ( IM.Bids.size() > 200 ) // if we won't filter many bids anyway, don't take the chance of filtering
				{
		//			IM.Bids = OM.FilterBids(IM.Bids, DesiredBidcount);
				}
				
				//System.out.println(IM.Bids.size());
				
			}
		
		
		// on the first time we act offer max bid
		if (previousBid == 0)
		{
			try { IM.AddMyBidToStatistics (IM.getMaxBid()); } catch (Exception e2) {}
			//return new Offer(this.getAgentID(),this.IM.getMaxBid());
			actionsToPerform.addAll(super.sendCounterOffer(offer, IM.getMaxBid() ));
		}
		
		double myBid;
		if (FirstActions >= 0 && getNormalisedTime(getStartTime()) < 0.15)
		{
			// on first few bids let the opponent learn some more about our preferences
			
			double utilDecrease = (1 - 0.925) / TotalFirstActions;
			
			myBid = IM.GenerateBidWithAtleastUtilityOf(0.925 + utilDecrease * FirstActions);
			--FirstActions;
		}
		else
		{
			myBid = IM.GenerateBidWithAtleastUtilityOf ( IM.GetNextRecommendedOfferUtility());
			
			if (IM.InFrenzy == true)
				myBid = IM.BestEverOpponentBid;
		}
		
		try { IM.AddMyBidToStatistics (myBid); } catch (Exception e2) {}
		
		log("sending offer : " + myBid);
		log("with util of : "+ getUtility(myBid));
		//catch (Exception e) {e.printStackTrace();}
		
		//return new Offer(this.getAgentID(), myBid );
		actionsToPerform.addAll(super.sendCounterOffer(offer, myBid ));
	}
	}
		return actionsToPerform;		
}



//	@Override
//	public AgentID getAgentID() {
//		// TODO Auto-generated method stub
//		return new AgentID("Gahboninho");
//	}

/**
 * Returns the state of the specified dialogue.
 * 
 * Casts it to the appropriate type to avoid having to cast each time.
 * 
 * @param dialogueId  the id of the dialogue for which to get state
 * @return state  the object representing the specified dialogue's state
 */
private DialogueStateANAC getDialogState(String dialogueId) {
    return (DialogueStateANAC) getDialogues().get(dialogueId);
}    

	  @Override
		protected double generateNextOffer(String dialogueId) {		
			return getInitialPrice() -
	                getRandom().nextInt(getInitialPrice() - getReservationPrice());
		}
	    
	    
	    /**
	     * Prints the contents of the matrix.
	     * 
	     * @param matrix
	     */
	    private void log(Matrix matrix) {
	        if (getLogLevel() >= Logger.STANDARD) {
	        	matrix.print(7, 4);
	        }
	    }
	    
	    /**
	     * Prints the contents of the matrix if the logging level is set to
	     * {@link Logger#MICRO} or higher.
	     * 
	     * @param matrix
	     */
	    private void logMicro(Matrix matrix) {
	        if (getLogLevel() >= Logger.MICRO) {
	        	log("sfkljsfl");
	            log(matrix);
	        }
	    }
}
