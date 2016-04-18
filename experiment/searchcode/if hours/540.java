package com.crimsonpig.fs.service.generate;

import com.crimsonpig.fs.domain.flightplan.Repetition;

public class RepetitionReducer {

	public Repetition buildRepetitionFromFrequency(Repetition originalRepetition, int flightFrequency){
		Repetition toReturn = originalRepetition;
		if(Repetition.FOUR_HOURS.getFrequency() == flightFrequency || 5 == flightFrequency){
			toReturn = Repetition.FOUR_HOURS;
		} else if(Repetition.SIX_HOURS.getFrequency() == flightFrequency){
			toReturn = Repetition.SIX_HOURS;
		} else if(Repetition.EIGHT_HOURS.getFrequency() == flightFrequency){
			toReturn = Repetition.EIGHT_HOURS;
		} else if(Repetition.TWELVE_HOURS.getFrequency() == flightFrequency){
			toReturn = Repetition.TWELVE_HOURS;
		} else if(Repetition.TWENTY_FOUR_HOURS.getFrequency() == flightFrequency){
			toReturn = Repetition.TWENTY_FOUR_HOURS;
		}
		return toReturn;
	}

}

