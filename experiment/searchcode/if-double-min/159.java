<<<<<<< HEAD
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
=======
/**
 * Copyright (c) 2002, Raben Systems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the Raben Systems, Inc. nor the names of its contributors
 * may be used to endorse or promote products derived from this software without
 * specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.ecommerce.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Routines for calculating and setting Julian day number
 * based on algorithms from Jean Meeus,
 * "Astronomical Algorithms", 2nd Edition, Willmann-Bell, Inc.,
 * 1998.
 *
 * @author Vern Raben (mailto:vern@raben.com)
 * @version $Revision: 1.16 $ $Date: 2002/07/22 14:24:17 $
 */
public final class JulianDay implements java.io.Serializable, Cloneable {
    public final static int JD = 100;
    public final static int MJD = 101;
    public final static int YEAR = Calendar.YEAR;
    public final static int MONTH = Calendar.MONTH;
    public final static int DATE = Calendar.DATE;
    public final static int HOUR = Calendar.HOUR;
    public final static int HOUR_OF_DAY = Calendar.HOUR_OF_DAY;
    public final static int MINUTE = Calendar.MINUTE;
    public final static int SECOND = Calendar.SECOND;
    public final static int DAY_OF_YEAR = Calendar.DAY_OF_YEAR;
    public final static int DAY_OF_WEEK = Calendar.DAY_OF_WEEK;
    public final static int DAY_OF_MONTH = Calendar.DAY_OF_MONTH;
    public final static int JANUARY = Calendar.JANUARY;
    public final static int FEBRUARY = Calendar.FEBRUARY;
    public final static int MARCH = Calendar.MARCH;
    public final static int APRIL = Calendar.APRIL;
    public final static int MAY = Calendar.MAY;
    public final static int JUNE = Calendar.JUNE;
    public final static int JULY = Calendar.JULY;
    public final static int AUGUST = Calendar.AUGUST;
    public final static int SEPTEMBER = Calendar.SEPTEMBER;
    public final static int OCTOBER = Calendar.OCTOBER;
    public final static int NOVEMBER = Calendar.NOVEMBER;
    public final static int DECEMBER = Calendar.DECEMBER;
    public final static String[] MONTHS = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
    public final static String[] TIME_UNIT = {"unk", "yr", "mo", "unk", "unk", "day", "unk", "unk", "unk", "unk", "unk", "hr", "min", "sec"};
    public final static double EPOCH_1970 = 2440587.5;
    public final static String SQL_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private DateFormat dateFormat = null;
    private Integer year = new Integer(0);
    private Integer month = new Integer(0);
    private Integer date = new Integer(0);
    private Integer hour = new Integer(0);
    private Integer minute = new Integer(0);
    private Integer second = new Integer(0);
    private Double jd;
    private Double mjd;
    private Integer dayOfWeek;
    private Integer dayOfYear;
    private final static DecimalFormat fmt4Dig = new DecimalFormat("0000");
    private final static DecimalFormat fmt2Dig = new DecimalFormat("00");
    private final static TimeZone tz = TimeZone.getTimeZone("UTC");

    /**
     * JulianCalendar constructor - sets JD for current time
     */
    public JulianDay() {
        Calendar cal = new GregorianCalendar(tz);
        setTime(cal.getTime());
    }

    /**
     * JulianCalendar constructor - sets JD passed as double
     *
     * @param jd double The Julian date
     */
    public JulianDay(double jd) {
        set(JulianDay.JD, jd);
        calcCalDate();
    }

    /**
     * Constructor to create Julian day given year, month, and decimal day
     *
     * @param yr int
     * @param mo int
     * @param da double
     */
    public JulianDay(int yr, int mo, double da) {
        int day = (int) da;
        int hr = 0;
        int min = 0;
        int sec = 0;
        double dhr = (da - day) * 24.0;
        hr = (int) dhr;
        double dmin = (dhr - hr) * 60.0;
        min = (int) (dmin);
        sec = (int) ((dmin - min) * 60.0);
        set(yr, mo, day, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, and date
     *
     * @param yr int
     * @param mo int
     * @param da int
     */
    public JulianDay(int yr, int mo, int da) {
        int hr = 0;
        int min = 0;
        int sec = 0;

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, date, hour and minute
     *
     * @param yr int
     * @param mo int
     * @param da int
     */
    public JulianDay(int yr, int mo, int da, int hr, int min) {

        int sec = 0;

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDate given year, month, day, hour, minute, and second
     *
     * @param yr  int
     * @param mo  int
     * @param da  int
     * @param hr  int
     * @param min int
     * @param sec int
     */
    public JulianDay(int yr, int mo, int da, int hr, int min, int sec) {

        if (da < 1) {
            da = 1;
        }

        if (mo < 0) {
            mo = 0;
        }

        if (hr < 0) {
            hr = 0;
        }

        if (min < 0) {
            min = 0;
        }

        if (sec < 0) {
            sec = 0;
        }

        set(yr, mo, da, hr, min, sec);
        calcJD();
    }

    /**
     * Construct JulianDay from system time in milli-seconds since Jan 1, 1970
     *
     * @param timeInMilliSec long
     */
    public JulianDay(long timeInMilliSec) {
        setDateTime("1970-01-01 0:00");
        add(JulianDay.DATE, ((double) timeInMilliSec / 86400000.0));
    }

    /**
     * Copy constructor for JulianDate
     *
     * @param cal com.raben.util.JulianDate
     */
    public JulianDay(JulianDay cal) {
        if (cal != null) {
            set(Calendar.YEAR, cal.get(Calendar.YEAR));
            set(Calendar.MONTH, cal.get(Calendar.MONTH));
            set(Calendar.DATE, cal.get(Calendar.DATE));
            set(Calendar.HOUR_OF_DAY, cal.get(Calendar.HOUR_OF_DAY));
            set(Calendar.MINUTE, cal.get(Calendar.MINUTE));
            set(Calendar.SECOND, cal.get(Calendar.SECOND));
            calcJD();
        } else {
            Calendar calendar = new GregorianCalendar(tz);
            setTime(calendar.getTime());
        }
    }

    /**
     * Set JulianDay from sql database compatible date/time string (yyyy-mm-dd hh:mm:ss)
     *
     * @param str java.lang.String
     */
    public JulianDay(String str) {
        setDateTime(str);
        calcJD();
    }

    /**
     * Construct JulianDate given Calendar as a parameter
     *
     * @param cal java.util.Calendar
     */
    public JulianDay(Calendar cal) {
        set(YEAR, cal.get(YEAR));
        set(MONTH, cal.get(MONTH));
        set(DATE, cal.get(DATE));
        set(HOUR_OF_DAY, cal.get(HOUR_OF_DAY));
        set(MINUTE, cal.get(MINUTE));
        set(SECOND, cal.get(SECOND));
        calcJD();
        calcCalDate();
    }

    /**
     * Add specified value in specified time unit to current Julian Date
     * increments next higher field
     * ISSUE - meaning of incrementing YEAR and MONTH by fractional value is not clear since
     * period of a month and year varies, that is ignored. Year is assumed to be 365 days and
     * month is assumed to be 30 days for computing the fractional increment.
     * ISSUE - not thoroughly tested, typically 1-2 second errors may occur
     * due to round-off. Will be refactored
     * "real soon  now" :) to utilize BigDecimal internal representation
     * of Julian Day.
     *
     * @param unit int Time unit
     * @param val  int Time increment
     */
    public void add(int unit, double val) {
        double da;

        switch (unit) {
            case YEAR:
                // issue - what this means if its not whole year
                int yr = year.intValue() + (int) val;
                set(YEAR, yr);
                da = (val - (int) val) * 365.0;
                set(DATE, da);
                break;
            case MONTH:
                int mo = month.intValue() + (int) val;
                set(MONTH, mo);
                da = (val - (int) val) * 30.0;
                set(DATE, da);
                break;

            case DATE:
                set(JD, getJDN() + val);
                break;
            case HOUR:
            case HOUR_OF_DAY:
                set(JD, getJDN() + (double) val / 24.0);
                break;
            case MINUTE:
                double min = minute.doubleValue() + val;
                set(JD, getJDN() + (double) val / 1440.0);
                break;
            case SECOND:
                double sec = second.doubleValue() + val;
                set(JD, getJDN() + (double) val / 86400.0);
                break;
            default:
                System.out.println("Error: JulianDate.add: The 'unit' parameter is not recognized=" + unit);
                set(JD, getJDN() + val);
                break;
        }

        calcJD();

    }

    /**
     * Add specified value in specified time unit to current Julian Date
     * increments next higher field
     * <p/>
     * ISSUE - meaning of incrementing YEAR and MONTH by fractional value is not clear since
     * period of a month and year varies, that is ignored. Year is assumed to be 365 days and
     * month is assumed to be 30 days for computing the fractional increment.
     * ISSUE - not thoroughly tested, typically 1-2 second errors may occur
     * due to round-off. Will be refactored
     * "real soon  now" :) to utilize BigDecimal internal representation
     * of Julian Day.
     *
     * @param unit int Time unit
     * @param val  int Time increment
     */
    public void add(int unit, int val) {
        int yr;
        int mo;
        switch (unit) {
            case YEAR:
                yr = year.intValue() + val;
                set(YEAR, yr);
                break;
            case MONTH:
                mo = month.intValue() + val;

                while (mo >= 12) {
                    mo -= 12;
                    yr = year.intValue() + 1;
                    set(YEAR, yr);
                }

                while (mo < 0) {
                    mo += 12;
                    yr = year.intValue() - 1;
                    set(YEAR, yr);
                }

                set(MONTH, mo);
                break;

            case DATE:
                set(JD, getJDN() + val);
                break;
            case HOUR:
            case HOUR_OF_DAY:
                set(JD, getJDN() + val * 0.041667);
                break;

            case MINUTE:
                set(JD, getJDN() + (double) val / 1440.0);
                break;

            case SECOND:
                set(JD, getJDN() + (double) val / 86400.0);
                break;
            default:
                System.out.println("Error: JulianDate.add: The 'unit' parameter is not recognized=" + unit);
                set(JD, getJDN() + val); // default to adding days
                break;
        }

        calcJD();

    }

    /**
     * Calculate calendar date for Julian date field this.jd
     */
    private void calcCalDate() {

        Double jd2 = new Double(jd.doubleValue() + 0.5);
        long I = jd2.longValue();
        double F = jd2.doubleValue() - (double) I;
        long A = 0;
        long B = 0;

        if (I > 2299160) {
            Double a1 = new Double(((double) I - 1867216.25) / 36524.25);
            A = a1.longValue();
            Double a3 = new Double((double) A / 4.0);
            B = I + 1 + A - a3.longValue();
        } else {
            B = I;
        }

        double C = (double) B + 1524;
        Double d1 = new Double((C - 122.1) / 365.25);
        long D = d1.longValue();
        Double e1 = new Double(365.25 * (double) D);
        long E = e1.longValue();
        Double g1 = new Double((double) (C - E) / 30.6001);
        long G = g1.longValue();
        Double h = new Double((double) G * 30.6001);
        long da = (long) C - E - h.longValue();
        date = new Integer((int) da);

        if (G < 14L) {
            month = new Integer((int) (G - 2L));
        } else {
            month = new Integer((int) (G - 14L));
        }

        if (month.intValue() > 1) {
            year = new Integer((int) (D - 4716L));
        } else {
            year = new Integer((int) (D - 4715L));
        }

        // Calculate fractional part as hours, minutes, and seconds
        Double dhr = new Double(24.0 * F);
        hour = new Integer(dhr.intValue());
        Double dmin = new Double((dhr.doubleValue() - (double) dhr.longValue()) * 60.0);
        minute = new Integer(dmin.intValue());
        Double dsec = new Double((dmin.doubleValue() - (double) dmin.longValue()) * 60.0);
        second = new Integer(dsec.intValue());

    }

    /**
     * Calculate day of week class attribute for class attribute jd
     */
    private void calcDayOfWeek() {
        JulianDay nJd = new JulianDay(getJDN());
        nJd.setStartOfDay();
        double nJdn = nJd.getJDN() + 1.5;
        int dow = (int) (nJdn % 7);
        dayOfWeek = new Integer(dow);
    }

    /**
     * Calculate day of year for jd (jd is a class attribute)
     */
    private void calcDayOfYear() {
        JulianDay julCal = new JulianDay();
        julCal.set(year.intValue(), 0, 1);
        double doy = jd.doubleValue() - julCal.getJDN();
        int idoy = (int) doy;
        dayOfYear = new Integer(idoy);
    }

    /**
     * Calculate Julian Date class attribute for class attributes year, month,
     * date, hour, minute, and second
     */
    private void calcJD() {
        int mo = month.intValue() + 1;
        int da = date.intValue();
        int yr = year.intValue();
        int A = 0;
        int B = 0;
        int C = 0;
        int D = 0;

        if (mo <= 2) {
            yr--;
            mo += 12;
        } else {
            mo = month.intValue() + 1;
        }

        if ((year.intValue() > 1582) || ((year.intValue() == 1582) && (month.intValue() >= 10) && (date.intValue() >= 15))) {
            Double a1 = new Double((double) yr / 100.0);
            A = a1.intValue();
            Double b1 = new Double((double) A / 4.0);
            B = 2 - A + b1.intValue();
        } else {
            B = 0;
        }

        Double c1 = new Double(365.25 * (double) yr);
        if (yr < 0) {
            c1 = new Double(365.25 * (double) yr - 0.75);
        }

        C = c1.intValue();
        Double d1 = new Double(30.6001 * (mo + 1));
        D = d1.intValue();

        double jdd = B + C + D + da + (hour.doubleValue() / 24.0) +
                (minute.doubleValue() / 1440.0) + (second.doubleValue() / 86400.0) +
                1720994.5;
        jd = new Double(jdd);

    }

    /**
     * Returns time difference in days between date specified and the JulianDay of this object
     * (parameter date-this date)
     *
     * @param date com.raben.util.JulianDate
     * @return double
     */
    public double diff(JulianDay date) {
        return date != null ? date.getJDN() - getJDN() : Double.NaN;
    }

    /**
     * Returns true if Julian day number is within 0.001 of parameter jd
     *
     * @param jd double
     * @return boolean
     */
    public boolean equals(double jd) {
        return Math.abs(jd - getJDN()) < 0.001 ? true : false;
    }

    /**
     * Return true if JulianDates are equal, false otherwise
     *
     * @param date com.raben.util.JulianDate
     * @return boolean
     */
    public boolean equals(JulianDay date) {
        boolean retVal = false;

        if (date != null) {
            retVal = equals(date.getJDN());
        }

        return retVal;

    }

    /**
     * Returns the specified field
     *
     * @param field int The specified field
     * @return int The field value
     */
    public final int get(int field) {

        switch (field) {
            case YEAR:
                return year.intValue();
            case MONTH:
                return month.intValue();
            case DAY_OF_MONTH:
                return date.intValue();
            case HOUR:
                int hr = hour.intValue();
                hr = hr > 12 ? hr -= 12 : hr;
                return hr;
            case HOUR_OF_DAY:
                return hour.intValue();
            case MINUTE:
                return minute.intValue();
            case SECOND:
                return second.intValue();
            case DAY_OF_WEEK:
                calcDayOfWeek();
                return dayOfWeek.intValue();
            case DAY_OF_YEAR:
                calcDayOfYear();
                return dayOfYear.intValue();
            default:
                return -1; // ISSUE - should throw exception? - what does Calendar do?
        }

    }

    /*
    * Get the UTC date/time string in the format yyyy-mm-dd hh:mm:ss
    * If the dateFormat is set, the date must be more recent than Jan 1, 1970
    * otherwise the empty string "" will be returned.)
    * @return java.lang.String
    */
    public String getDateTimeStr() {
        String retStr = "";

        if ((dateFormat != null) && (getJDN() >= EPOCH_1970)) {
            dateFormat.setTimeZone(tz);
            retStr = dateFormat.format(getTime());
        } else {
            StringBuffer strBuf = new StringBuffer(fmt4Dig.format(get(JulianDay.YEAR)));
            strBuf.append("-");
            strBuf.append(fmt2Dig.format(get(JulianDay.MONTH) + 1));
            strBuf.append("-");
            strBuf.append(fmt2Dig.format(get(JulianDay.DATE)));
            strBuf.append(" ");
            strBuf.append(fmt2Dig.format(get(JulianDay.HOUR_OF_DAY)));
            strBuf.append(":");
            strBuf.append(fmt2Dig.format(get(JulianDay.MINUTE)));
            strBuf.append(":");
            strBuf.append(fmt2Dig.format(get(JulianDay.SECOND)));
            retStr = strBuf.toString();
        }
        return retStr;
    }

    /**
     * Returns the Julian Date Number as a double
     *
     * @return double
     */
    public final double getJDN() {
        if (jd == null) {
            calcJD();
        }

        calcJD();

        return jd.doubleValue();
    }

    /**
     * Returns milli-seconds since Jan 1, 1970
     *
     * @return long
     */
    public long getMilliSeconds() {
        //JulianDay jd1970=new JulianDay("1970-01-01 0:00");
        //double diff=getJDN()-jd1970.getJDN();
        double diff = getJDN() - EPOCH_1970;
        return (long) (diff * 86400000.0);
    }

    /**
     * Return the modified Julian date
     *
     * @return double
     */
    public final double getMJD() {

        return (getJDN() - 2400000.5);
    }

    /**
     * Return date as YYYYMMDDHHSS string with the least unit to be returned specified
     * For example to to return YYYYMMDD specify least unit as JulianDay.DATE
     *
     * @param leastUnit int least unit to be returned
     */
    public String getYMD(int leastUnit) {

        StringBuffer retBuf = new StringBuffer();
        int yr = get(JulianDay.YEAR);
        int mo = get(JulianDay.MONTH) + 1;
        int da = get(JulianDay.DATE);
        int hr = get(JulianDay.HOUR_OF_DAY);
        int min = get(JulianDay.MINUTE);
        int sec = get(JulianDay.SECOND);

        String yrStr = fmt4Dig.format(yr);

        String moStr = fmt2Dig.format(mo);
        String daStr = fmt2Dig.format(da);
        String hrStr = fmt2Dig.format(hr);
        String minStr = fmt2Dig.format(min);
        String secStr = fmt2Dig.format(sec);

        switch (leastUnit) {
            case JulianDay.YEAR:
                retBuf.append(yrStr);
                break;

            case JulianDay.MONTH:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                break;

            case JulianDay.DATE:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                break;

            case JulianDay.HOUR_OF_DAY:
            case JulianDay.HOUR:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                retBuf.append(hrStr);
                break;

            case JulianDay.MINUTE:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                retBuf.append(hrStr);
                retBuf.append(minStr);
                break;

            case JulianDay.SECOND:
                retBuf.append(yrStr);
                retBuf.append(moStr);
                retBuf.append(daStr);
                retBuf.append(hrStr);
                retBuf.append(minStr);
                retBuf.append(secStr);
                break;
        }

        return retBuf.toString();

    }

    /**
     * This method sets Julian day or modified Julian day
     *
     * @param field int Field to be changed
     * @param value double The value the field is set to
     *              ISSUE - double values are truncated when setting
     *              YEAR, MONTH<DATE, HOUR,MINUTE, and SECOND - this is not
     *              what should happen. (Should be able to set date to 1.5 to be
     *              the 1st day of month plus 12 hours).
     */
    public void set(int field, double value) {
        int ivalue = (int) value;

        switch (field) {

            case JD:
                jd = new Double(value);
                calcCalDate();
                break;

            case MJD:
                jd = new Double(value + 2400000.5);
                calcCalDate();
                break;

            case YEAR:
                year = new Integer(ivalue);
                calcJD();
                break;

            case MONTH:
                if (ivalue > 11) {
                    int yr = year.intValue() + 1;
                    set(YEAR, ivalue);
                    ivalue -= 11;
                }
                month = new Integer(ivalue);
                calcJD();
                break;

            case DATE:
                date = new Integer(ivalue);
                calcJD();
                break;

            case HOUR_OF_DAY:
            case HOUR:
                hour = new Integer(ivalue);
                while (hour.intValue() >= 24) {
                    add(DATE, 1);
                    hour = new Integer(hour.intValue() - 24);
                }
                calcJD();
                break;

            case MINUTE:
                minute = new Integer(ivalue);
                while (minute.intValue() >= 60) {
                    add(HOUR, 1);
                    minute = new Integer(minute.intValue() - 60);
                }
                calcJD();
                break;

            case SECOND:
                second = new Integer(ivalue);
                while (second.intValue() >= 60) {
                    add(MINUTE, 1);
                    second = new Integer(second.intValue() - 60);
                }
                calcJD();
                break;

        }

    }

    /**
     * Set various JulianCalendar fields
     * Example:
     * JulianDay jd=new JulianDay();
     * jd.set(Calendar.YEAR,1999);
     *
     * @param field int The field to be set
     * @param value int The field value
     */
    public final void set(int field, int value) {

        switch (field) {
            case YEAR:
                year = new Integer(value);
                break;

            case MONTH:
                month = new Integer(value);
                break;

            case DATE:
                date = new Integer(value);
                break;

            case HOUR_OF_DAY:
            case HOUR:
                hour = new Integer(value);
                break;

            case MINUTE:
                minute = new Integer(value);
                break;

            case SECOND:
                second = new Integer(value);
                break;
        }
        calcJD();

    }

    /**
     * Set year, month, and day
     *
     * @param year  int
     * @param month int Note - January is 0, December is 11
     * @param date  int
     */
    public final void set(int year, int month, int date) {
        this.year = new Integer(year);
        this.month = new Integer(month);
        this.date = new Integer(date);
        this.hour = new Integer(0);
        this.minute = new Integer(0);
        this.second = new Integer(0);
        calcJD();
    }

    /**
     * Set year, month,day, hour and minute
     *
     * @param year   int
     * @param month  int January is 0, Dec is 11
     * @param date   int
     * @param hour   int
     * @param minute int
     */
    public final void set(int year, int month, int date, int hour, int minute) {
        this.year = new Integer(year);
        this.month = new Integer(month);
        this.date = new Integer(date);
        this.hour = new Integer(hour);
        this.minute = new Integer(minute);
        this.second = new Integer(0);
        calcJD();
    }

    /**
     * Set year month, day, hour, minute and second
     *
     * @param year   int
     * @param month  int January is 0, December is 11
     * @param date   int
     * @param hour   int
     * @param minute int
     * @param second int
     */
    public final void set(int year, int month, int date, int hour, int minute, int second) {
        this.year = new Integer(year);
        this.month = new Integer(month);
        this.date = new Integer(date);
        this.hour = new Integer(hour);
        this.minute = new Integer(minute);
        this.second = new Integer(second);
        calcJD();
    }

    /**
     * Set date/time from string
     *
     * @param str java.lang.String
     */
    public void setDateTime(String str) {
        try {
            int vals[] = {0, 0, 0, 0, 0, 0};
            str = str.replace('T', ' ');
            StringTokenizer tok = new StringTokenizer(str, "/:- ");

            if (tok.countTokens() > 0) {

                // Check if its not a database time format yyyy-mm-dd
                int j = str.indexOf("-");

                if ((j == -1) && (tok.countTokens() == 1)) {
                    setYMD(str);
                } else {
                    int i = 0;

                    while (tok.hasMoreTokens()) {
                        vals[i++] = Integer.parseInt(tok.nextToken());
                    }

                    set(vals[0], vals[1] - 1, vals[2], vals[3], vals[4], vals[5]);

                }

            }

        } catch (NumberFormatException e) {
            throw new Error(e.toString());
        }

        calcJD();


    }

    /**
     * set hour to 23, minute and second to 59
     */
    public void setEndOfDay() {
        int yr = get(YEAR);
        int mo = get(MONTH);
        int da = get(DATE);
        set(yr, mo, da, 23, 59, 59);
    }

    /**
     * Set hour,minute, and second to 0
     */
    public void setStartOfDay() {
        int yr = get(YEAR);
        int mo = get(MONTH);
        int da = get(DATE);
        set(yr, mo, da, 0, 0, 0);
    }

    /**
     * Set date from Java Date
     *
     * @param dat java.util.Date
     */
    public final void setTime(Date dat) {
        Calendar cal = new GregorianCalendar(tz);
        cal.setTime(dat);
        year = new Integer(cal.get(Calendar.YEAR));
        month = new Integer(cal.get(Calendar.MONTH));
        date = new Integer(cal.get(Calendar.DATE));
        hour = new Integer(cal.get(Calendar.HOUR_OF_DAY));
        minute = new Integer(cal.get(Calendar.MINUTE));
        second = new Integer(cal.get(Calendar.SECOND));
        //System.out.println("JulianCalendar.setTime: year="+year+" month="+month+" date="+date+" hour="+hour+" minute="+minute+" second="+second);
        calcJD();
        //System.out.println("jd="+jd);
    }

    /**
     * Set date from sting in the form YYYYMMDDhhmmss (YYYY=year MM=month DD=day hh=hr mm=min ss=sec)
     *
     * @param str java.lang.String
     */
    public void setYMD(String str) {

        int vals[] = {0, 0, 0, 0, 0, 0};

        if (str.length() >= 4) {
            vals[0] = Integer.parseInt(str.substring(0, 4));
        }
        if (str.length() >= 6) {
            vals[1] = Integer.parseInt(str.substring(4, 6));
        }

        if (str.length() >= 8) {
            vals[2] = Integer.parseInt(str.substring(6, 8));
        }

        if (str.length() >= 10) {
            vals[3] = Integer.parseInt(str.substring(8, 10));
        }
        if (str.length() >= 12) {
            vals[4] = Integer.parseInt(str.substring(10, 12));
        }

        if (str.length() >= 14) {
            vals[5] = Integer.parseInt(str.substring(12, 14));
        }

        set(YEAR, vals[0]);
        set(MONTH, vals[1] - 1);
        set(DATE, vals[2]);
        set(HOUR_OF_DAY, vals[3]);
        set(MINUTE, vals[4]);
        set(SECOND, vals[5]);
    }

    public final String toString() {

        StringBuffer buf = new StringBuffer("JulianDay[jdn=");
        buf.append(getJDN());
        buf.append(",yr=");
        buf.append(get(Calendar.YEAR));
        buf.append(",mo=");
        buf.append(get(Calendar.MONTH));
        buf.append(",da=");
        buf.append(get(Calendar.DATE));
        buf.append(",hr=");
        buf.append(get(Calendar.HOUR_OF_DAY));
        buf.append(",min=");
        buf.append(get(Calendar.MINUTE));
        buf.append(",sec=");
        buf.append(get(Calendar.SECOND));
        buf.append(",dayOfWeek=");
        buf.append(get(DAY_OF_WEEK));
        buf.append(",dayOfYear=");
        buf.append(get(DAY_OF_YEAR));
        buf.append("]");

        return buf.toString();
    }

    /**
     * Return clone of JulianDay object
     *
     * @return Object;
     */
    public Object clone() {
        JulianDay clone = null;
        try {
            clone = (JulianDay) super.clone();
        }
        catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return clone;
    }

    /**
     * Set SimpleDateFormat string
     * ISSUE - only valid after Jan 1, 1970
     */
    public void setDateFormat(java.lang.String formatStr) {
        if ((formatStr != null) && (formatStr.length() > 0)) {
            dateFormat = new SimpleDateFormat(formatStr);
        }
    }

    /**
     * Set SimpleDateFormat for displaying date/time string
     *
     * @param dateFormat SimpleDateFormat
     */
    public void setDateFormat(SimpleDateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    /**
     * Return Java Date
     *
     * @return Date
     */
    public Date getTime() {
        return new Date(getMilliSeconds());
    }

    /**
     * Update JulianDay to current time
     */
    public void update() {
        Calendar cal = new GregorianCalendar(tz);
        setTime(cal.getTime());
    }

    /**
     * Get increment in days given time unit and increment
     *
     * @param unit Time unit (DATE,HOUR,HOUR_OF_DAY,MINUTE, or SECOND
     * @param incr Time increment in unit specified
     * @return double Increment in days
     * @throws unit is not Julian.DATE, HOUR, HOUR_OF_DAY, MINUTE or SECOND
     */
    public static double getIncrement(int unit, int incr) {
        double retVal = 0.0;

        switch (unit) {
            case DATE:
                retVal = incr;
                break;
            case HOUR:
            case HOUR_OF_DAY:
                retVal = incr / 24.0;
                break;
            case MINUTE:
                retVal = incr / 1440.0;
                break;
            case SECOND:
                retVal = incr / 86400.0;
                break;
            default:
                StringBuffer errMsg = new StringBuffer("JulianDay.getIncrement unit=");
                errMsg.append(unit);

                if ((unit > 0) && (unit < TIME_UNIT.length)) {
                    errMsg.append(" (");
                    errMsg.append(TIME_UNIT[unit]);
                    errMsg.append(" )");
                }

                throw new IllegalArgumentException(errMsg.toString());

        }

        return retVal;
    }

    /**
     * Get java Calendar equivalent of Julian Day
     *
     * @return Calendar
     */
    public java.util.Calendar getCalendar() {
        Calendar cal = GregorianCalendar.getInstance(tz);
        cal.set(get(YEAR), get(MONTH), get(DATE), get(HOUR_OF_DAY),
                get(MINUTE), get(SECOND));
        return cal;
    }

}

>>>>>>> 76aa07461566a5976980e6696204781271955163
