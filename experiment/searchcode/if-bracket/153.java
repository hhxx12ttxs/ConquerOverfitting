package com.carmel.tournamentpairing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregateBouts {

	List<File> files = null;
	List<String> bracketList = new ArrayList<String>();
	List<Wrestler> wrestlerList = new ArrayList<Wrestler>();
	List<String> lineList = new ArrayList<String>();
	Map<String, Integer> matchNumberMap = new HashMap<String, Integer>();
	
	public static final String roundOne = "RD#1";
	public static final String roundTwo = "RD#2";
	public static final String roundThree = "RD#3";
	public static final String roundFour = "RD#4";
	public static final String roundFive = "RD#5";
	
	public AggregateBouts(ArrayList<File> files){
		this.files = files;
	}
	
	public void aggregate(){
		initializeMap();
		for(File file: files){
			readFile(file);
		}
		printOutMasterList();
		createBouts();
		System.out.println("COMPLETE");
	}
	private void initializeMap(){
		matchNumberMap.put(roundOne, 1);
		matchNumberMap.put(roundTwo, 1);
		matchNumberMap.put(roundThree, 1);
		matchNumberMap.put(roundFour, 1);
		matchNumberMap.put(roundFive, 1);
	}
	private List<Wrestler> getWrestlers(int number){
		List<Wrestler> toReturn = new ArrayList<Wrestler>();
		for(int i=0;i<number;i++){
			toReturn.add(wrestlerList.remove(0));
		}
		return toReturn;
	}
	private void createBouts(){
		for(String bracket: bracketList){
			if(bracket.equals(BracketIntelligence.threeBracket)){
				matchNumberMap = BoutBuilder.createBoutCards(BracketIntelligence.threeBracket, getWrestlers(3), matchNumberMap);
			}
			else if(bracket.equals(BracketIntelligence.fourBracket)){
				matchNumberMap = BoutBuilder.createBoutCards(BracketIntelligence.fourBracket, getWrestlers(4), matchNumberMap);
			}
			else if(bracket.equals(BracketIntelligence.fiveBracket)){
				matchNumberMap = BoutBuilder.createBoutCards(BracketIntelligence.fiveBracket, getWrestlers(5), matchNumberMap);
			}
			else if(bracket.equals(BracketIntelligence.sixBracket)){
				matchNumberMap = BoutBuilder.createBoutCards(BracketIntelligence.sixBracket, getWrestlers(6), matchNumberMap);
			}
		}
	}
	private String isBoutDiscription(String line){
		if(line.indexOf(BracketIntelligence.threeBracket)!=-1){
			return BracketIntelligence.threeBracket;
		}
		else if(line.indexOf(BracketIntelligence.fourBracket)!=-1){
			return BracketIntelligence.fourBracket;
		}
		else if(line.indexOf(BracketIntelligence.fiveBracket)!=-1){	
			return BracketIntelligence.fiveBracket;
		}
		else if(line.indexOf(BracketIntelligence.sixBracket)!=-1){
			return BracketIntelligence.sixBracket;
		}
		else{
			return "";
		}
	}
	private void readFile(File afile){
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(afile));
			String currLine = null;
			while((currLine = reader.readLine())!= null){
				lineList.add(currLine);
				String bracketType = isBoutDiscription(currLine);
				if(bracketType==""){
					//First, Last, Weight, School, Grade
					String[] split = currLine.split(BracketIntelligence.seperator);
					String first = split[0];
					String last = split[1];
					String weight = split[2];
					String school = split[3];
					String grade = split[4];
					Wrestler.WrestlerBuilder builder = new Wrestler.WrestlerBuilder().first(first)
							.last(last).weight(weight).school(school).grade(grade);
					Wrestler newWrestler = new Wrestler(builder);
					wrestlerList.add(newWrestler);
				}
				else{
					bracketList.add(bracketType);
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try{
				if(reader != null){
					reader.close();
				}
			} catch (IOException e){
				e.printStackTrace();
			}
		}
	}
	private void printOutMasterList(){
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter("MasterBoutList.txt"));
			int count = 0;
			for(String line :lineList){
				if(count!=lineList.size()-1){
					writer.write(line + MainApp.newLine);
				}
				else{
					writer.write(line);
				}
				count++;
			}
		} catch (IOException e) {
			System.out.println("Error creating master list");
		} finally{
			if(writer != null){
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
		}
	}
}

