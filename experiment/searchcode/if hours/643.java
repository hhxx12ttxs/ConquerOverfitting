package com.araeosia.ArcherGames.utils;

public class Time {
	public static String getString(int Seconds){
		String output="";
		if(Seconds/3600>=1){
			// There's hours.
			int Hours = (int) Math.floor(Seconds/3600);
			if(Hours>1){
				output = output+" "+Hours+" hours";
			}else{
				output = output+" "+Hours+" hour";
			}
			Seconds=(Seconds-(Hours*3600));
		}
		if(Seconds/60>=1){
			// There's minutes.
			int Minutes = (int) Math.floor(Seconds/60);
			if(Minutes>1){
				output = output+" "+Minutes+" minutes";
			}else{
				output = output+" "+Minutes+" minute";
			}
			Seconds=(Seconds-(Minutes*60));
		}
		if(Seconds>=1){
			// There's seconds.
			if(Seconds>1){
				output = output+" "+Seconds+" seconds";
			}else{
				output = output+" "+Seconds+" second";
			}
		}
		return output;
	}
	public static String getShortString(int Seconds){
		String output="";
		if(Seconds/3600>=1){
			// There's hours.
			int Hours = (int) Math.floor(Seconds/3600);
			if(Hours>1){
				return Hours+" hours";
			}else{
				return Hours+" hour";
			}
		}
		if(Seconds/60>=1){
			// There's minutes.
			int Minutes = (int) Math.floor(Seconds/60);
			if(Minutes>1){
				return Minutes+" minutes";
			}else{
				return Minutes+" minute";
			}
		}
		if(Seconds>=1){
			// There's seconds.
			if(Seconds>1){
				return Seconds+" seconds";
			}else{
				return Seconds+" second";
			}
		}
		return output;
	}
}

