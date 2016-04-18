/*
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package org.hummer.kickstalker.util;

/**
 * @author gernot.hummer
 *
 * @version 1.0
 *
 */
public class TimeUtil {

	public static final int HOURS_A_DAY = 24;
	
	public static String hoursToReadable(int hours){
		
		if(hours > HOURS_A_DAY){
			
			return Float.valueOf(
					Math.round(((float) hours) / 24)).intValue() + " d";
			
		} else if(hours > 0){
			
			return hours + " h";
			
		} else {
			
			return "Ended";
			
		}
		
	}
	
}

