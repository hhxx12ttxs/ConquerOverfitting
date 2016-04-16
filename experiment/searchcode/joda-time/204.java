package backend;

import java.util.*;

import json.*;

import wattball.*;

public class WattballScheduler {

	private WattballInterop connection;

	public WattballScheduler() {
		connection = new WattballInterop();
	}

	public boolean schedule() {
		
		connection = new WattballInterop();

		ArrayList<JSONObject> teamsList = connection.getModelList("Wattballteam", "Wattballteams");
		
		ArrayList<JSONObject> team1List = new ArrayList<JSONObject>();
		ArrayList<JSONObject> team2List = new ArrayList<JSONObject>();

		ArrayList<JSONObject> rotatedTeam1List = new ArrayList<JSONObject>();
		ArrayList<JSONObject> rotatedTeam2List = new ArrayList<JSONObject>();

		ArrayList<JSONObject> completeScheduledSet = new ArrayList<JSONObject>();
		ArrayList<Integer> teamIDs = new ArrayList<Integer>();
		
		ArrayList<Integer> dayOneTeamIDs = new ArrayList<Integer>(); //Stores id's for day one
		ArrayList<Integer> dayTwoTeamIDs = new ArrayList<Integer>(); //Stores id's for day two
		ArrayList<Integer> dayThreeTeamIDs = new ArrayList<Integer>(); //Stores id's for day three
		
		
		for(int i = 0; i < teamsList.size(); i++){ 

				if(i <= teamsList.size() / 2 - 1){ 
	
					team1List.add(teamsList.get(i));
					
				} else { 
	
					team2List.add(teamsList.get(i)); 
				}
		}
		

		for(int a = 0; a < team1List.size(); a++) { 

			rotatedTeam1List.addAll(team1List); 

		}

			
		for(int b = 0; b < team2List.size(); b++) { 

			final int j = 1 ; 

			Collections.rotate(team2List, j); 
			
			rotatedTeam2List.addAll(team2List); 

		}

		for(int c = 0; c < rotatedTeam1List.size() && c < rotatedTeam2List.size(); c++) { 

			completeScheduledSet.add(rotatedTeam1List.get(c));
			completeScheduledSet.add(rotatedTeam2List.get(c));
		}
		
		for(JSONObject jobj : completeScheduledSet) {
			
			int id = jobj.getInt("id"); //Take all id's from the JSONObject and put in own arrayList
			teamIDs.add(id);
		}
		
		for(int i = 0; i < teamIDs.size(); i++) {
			
			if(i < 20) { //splits groupings into third section
				
				dayOneTeamIDs.add(teamIDs.get(i));
			}
			
			else if(i < 40) { //splits groupings into second section
			
				dayTwoTeamIDs.add(teamIDs.get(i));
			}
			
			else { //splits groupings into third section
				
				dayThreeTeamIDs.add(teamIDs.get(i));
			}
		}
		
		String scheduleDay = "data[Wattballmatch][scheduleday_id]=";  //used to concatenate strings together
		String teamOneID = "&data[Wattballmatch][wattballteam1_id]="; //in a loop 
		String teamTwoID = "&data[Wattballmatch][wattballteam2_id]="; 
		String timeSlot = "&data[Wattballmatch][time_slot]="; //For the times, 90 minutes each
		
		String colon = ":"; //for time slot
		
		String firstQuotes = "\""; //Chucks in the quotes at front
		String lastQuotes = "\""; //Chucks in the quotes at back
		
		int hours = 9;
		int minutes = 0;
		int seconds = 0;
		
		final int dayOne = 1; //constants
		final int dayTwo = 2; //for each
		final int dayThree = 3; //day of tournament
		
		String wattDay1 = "";
		
		for(int even = 0, odd = 1; even < dayOneTeamIDs.size() && odd < dayOneTeamIDs.size(); even = even + 2, odd = odd +2){
			
			wattDay1 += firstQuotes + scheduleDay + dayOne + teamOneID + teamIDs.get(even) + teamTwoID + teamIDs.get(odd) + timeSlot + hours + colon + minutes + seconds + colon + seconds + seconds + lastQuotes;
			
				System.out.println(firstQuotes + scheduleDay + dayOne + teamOneID + teamIDs.get(even) + teamTwoID + teamIDs.get(odd) + timeSlot + hours + colon + minutes + seconds + colon + seconds + seconds + lastQuotes);
				hours = hours + 1;
				minutes = minutes + 3;
				if(minutes >= 6){
					minutes = 0;
					hours = hours + 1;
				}
		}
		
		hours = 9;
		minutes = 0;
		seconds = 0;
		
		System.out.println();
		System.out.println();
		
		String wattDay2 = "";
		
		for(int even = 0, odd = 1; even < dayTwoTeamIDs.size() && odd < dayTwoTeamIDs.size(); even = even + 2, odd = odd +2){
			
			wattDay2 += firstQuotes + scheduleDay + dayTwo + teamOneID + teamIDs.get(even) + teamTwoID+teamIDs.get(odd) + timeSlot + hours + colon + minutes + seconds + colon + seconds + seconds + lastQuotes;
			
			System.out.println(firstQuotes + scheduleDay + dayTwo + teamOneID + teamIDs.get(even) + teamTwoID+teamIDs.get(odd) + timeSlot + hours + colon + minutes + seconds + colon + seconds + seconds + lastQuotes);
			hours = hours + 1;
			minutes = minutes + 3;
			if(minutes >= 6){
				minutes = 0;
				hours = hours + 1;
			}
		}
		
		hours = 9;
		minutes = 0;
		seconds = 0;
		
		System.out.println();
		System.out.println();
		
		String wattDay3 = "";
		
		for(int even = 0, odd = 1; even < dayThreeTeamIDs.size() && odd < dayThreeTeamIDs.size(); even = even + 2, odd = odd +2){
			
			wattDay3 += firstQuotes + scheduleDay + dayThree + teamOneID + teamIDs.get(even) + teamTwoID + teamIDs.get(odd) + timeSlot + hours + colon + minutes + seconds + colon + seconds + seconds + lastQuotes;
			
			System.out.println(firstQuotes + scheduleDay + dayThree + teamOneID + teamIDs.get(even) + teamTwoID + teamIDs.get(odd) + timeSlot + hours + colon + minutes + seconds + colon + seconds + seconds + lastQuotes);
			hours = hours + 1;
			minutes = minutes + 3;
			if(minutes >= 6){
				minutes = 0;
				hours = hours + 1;
			}
		}
		
		System.out.println();
		System.out.println();

		String finalScheduledSet = completeScheduledSet.toString().replace("[", "").replace("]", ""); //Replaces array bars to make back into JSON
		String x = "Wattballteam"; //Used to turn back into JSON
		
		//System.out.println();
		//System.out.println("### OUTPUT OF WATTTEAMS ###");
		//System.out.println(teamsList);
		//System.out.println();
		//System.out.println("### OUTPUT OF TEAMS1DATA ###");
		//System.out.println(team1List);
		//System.out.println();
		//System.out.println("### OUTPUT OF TEAMS2DATA ###");
		//System.out.println(team2List);
		//System.out.println();
		//System.out.println("### OUTPUT OF ROTATED TEAMS1DATA ###");
		//System.out.println(rotatedTeam1List);
		//System.out.println();
		//System.out.println("### OUTPUT OF ROTATED TEAMS2DATA ###");
		//System.out.println(rotatedTeam2List);
		//System.out.println();
		//System.out.println("### OUTPUT OF COMPLETE SCHEDULE ###");
		//System.out.println(completeScheduledSet);
		//System.out.println();
		System.out.println("### OUTPUT OF COMPLETE SCHEDULED SET IN JSON OBJECT FORM ###");
		System.out.println("[{" + "\""+ x +"\"" + ":" + finalScheduledSet);
		System.out.println();
		//System.out.println("### OUTPUT OF WATTBALL MATCHES ###");
		//System.out.println(teamScheduledPlay);
		//System.out.println();
		System.out.println("### OUTPUT OF WATTBALL MATCHES ID'S ###");
		System.out.println(teamIDs);
		System.out.println();
		//System.out.println("### OUTPUT OF WATTBALL MATCHES FOR DAY ONE ###");
		//System.out.println(dayOneTeamIDs);
		//System.out.println();
		//System.out.println("### OUTPUT OF WATTBALL MATCHES FOR DAY TWO ###");
		//System.out.println(dayTwoTeamIDs);
		//System.out.println();
		//System.out.println("### OUTPUT OF WATTBALL MATCHES FOR DAY THREE ###");
		//System.out.println(dayThreeTeamIDs);
		//System.out.println();
		//System.out.println("### STRING ###");
		//System.out.println(wattDay3);
		//System.out.println();

		return false;

	}
}

