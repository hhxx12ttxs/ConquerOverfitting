package trainingDataGenerator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple class to run through a directory of evaluations and rank each one three
 * rankings
 * 
 * 1. Precision
 * 2. Recall
 * 3. F-Measure
 * @author mhotan
 *
 */
public class EvaluationRanker {

	/**
	 * Users need to input the main directory where all the evaluations are stored
	 * <b> Should be a directory of text files produced by ClassifierEvaluator a series of times
	 * 
	 * Out
	 */
	private static final String USAGE = "{Usage: <evalsDir> <outputDir> }";

	private static final String OUTPUT_OVERALL = "overrall_results.txt";
	private static final String OUTPUT_TOPF = "top_fmeasure.txt";
	private static final String OUTPUT_PREC = "top_precision.txt";
	private static final String OUTPUT_RECALL = "top_recall.txt";
	private static final String SUMMARY = "Summary";
	private static final String PRECISION = "Precision";
	private static final String RECALL = "Recall";
	private static final String F1 = "F1-Measure";
	
	public static void main(String[] args) throws Exception{
		if (args.length != 2){
			printUsage("");
			return;
		}

		String evalDirPath = args[0];
		String outPutDir = args[1];

		if (evalDirPath == null || outPutDir == null)
			printUsage("Null input argument");
		
		File evalDir = new File(evalDirPath);
		if (!evalDir.isDirectory())
			printUsage("evalsDir is not a directory: " + evalDirPath);
		
		File outputDirectory = new File(outPutDir);
		if (outputDirectory.exists() && !outputDirectory.isDirectory())
			printUsage("outputDir exist and is not a directory" + outPutDir);
		if (!outputDirectory.exists()) {
			System.out.println("Creating directory at " + outPutDir);
			if (!outputDirectory.mkdir())
				printUsage("Unable to make directory at " + outPutDir);
		}
		File [] subFiles = evalDir.listFiles();
		List<Rank> precisionRanks = new ArrayList<Rank>(subFiles.length);
		List<Rank> recallRanks = new ArrayList<Rank>(subFiles.length);
		List<Rank> fmeasureRanks = new ArrayList<Rank>(subFiles.length);
		
		for (File subFile : subFiles) {
			if (!subFile.getAbsolutePath().endsWith(".txt"))
				continue;
			Rank r = processFile(subFile);
			if (r == null){
				System.err.println("Unable to find 3 rankings from: " + subFile.getName());
				continue;
			}
				
			precisionRanks.add(r);
			recallRanks.add(r);
			fmeasureRanks.add(r);
		}
		
		// Sort by best 
		Collections.sort(precisionRanks, Rank.PrecisionComparator);
		Collections.sort(recallRanks, Rank.RecallComparator);
		Collections.sort(fmeasureRanks, Rank.FMeasureComparator);
		
		// Create 4 files
		// Overrall scores
		// 3 individual file that list all of them
		// Write output
		File overall =  new File(outputDirectory, OUTPUT_OVERALL);
		if (overall.exists()){
			overall.delete();
		}
		overall.createNewFile();
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(overall));
		writer.write("Overall Results");
		writer.newLine();
		writer.newLine();
		writer.write("Top F1 Measure Results:");
		writer.newLine();
		for (int i = 0; i < 10  && i < fmeasureRanks.size(); ++i){
			Rank r = fmeasureRanks.get(i);
			writer.write((i+1) + ". " + r.name +" F1-Measure: " + r.fmeasure + " Precision: " + r.precision +  " Recall: " + r.recall);
			writer.newLine();
		}
		writer.newLine();
		writer.write("Top Precision Results:");
		writer.newLine();
		for (int i = 0; i < 10  && i < precisionRanks.size(); ++i){
			Rank r = precisionRanks.get(i);
			writer.write((i+1) + ". " + r.name + " Precision: " + r.precision +  " Recall: " + r.recall + " F1-Measure: " + r.fmeasure);
			writer.newLine();
		}
		
		writer.newLine();
		writer.write("Top Recall Results:");
		writer.newLine();
		for (int i = 0; i < 10  && i < recallRanks.size(); ++i){
			Rank r = recallRanks.get(i);
			writer.write((i+1) + ". " + r.name + " Recall: " + r.recall + " Precision: " + r.precision +  " F1-Measure: " + r.fmeasure);
			writer.newLine();
		}
		writer.close();
		
		// Write other three images
		writeOutputFile(outputDirectory, OUTPUT_TOPF, fmeasureRanks);
		writeOutputFile(outputDirectory, OUTPUT_PREC, precisionRanks);
		writeOutputFile(outputDirectory, OUTPUT_RECALL, recallRanks);
		
		System.out.println("Completed Ranking...");
	}
	
	private static void writeOutputFile(File outputDir, String outputName, List<Rank> ranks) throws IOException{
		File outFile =  new File(outputDir, outputName);
		if (outFile.exists()){
			outFile.delete();
		}
		outFile.createNewFile();
		
		String title = null;
		if (outputName.equals(OUTPUT_TOPF)) {
			title = F1;
		} else if (outputName.equals(OUTPUT_PREC)) {
			title = PRECISION;
		} else if (outputName.equals(OUTPUT_RECALL)){
			title = RECALL;
		}
		title += " Results";
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
		writer.write(title);
		writer.newLine();
		
		int i = 1;
		for (Rank r: ranks){
			String toWrite = null;
			if (outputName.equals(OUTPUT_TOPF)) {
				toWrite = i + ". " + r.name +" F1-Measure: " + r.fmeasure +
						" Precision: " + r.precision +  " Recall: " + r.recall;
			} else if (outputName.equals(OUTPUT_PREC)) {
				toWrite = i + ". " + r.name + " Precision: " + r.precision + 
						" Recall: " + r.recall + " F1-Measure: " + r.fmeasure;
			} else if (outputName.equals(OUTPUT_RECALL)){
				toWrite = i + ". " + r.name + " Recall: " + r.recall + 
						" Precision: " + r.precision +  " F1-Measure: " + r.fmeasure;
			}
			if (toWrite != null){
				writer.write(toWrite);
				writer.newLine();
				i++;
			}
			
		}
		writer.close();
	}

	/**
	 * Processes for extracting fmeasure, recall, and precision from a text file
	 * created by ClassifierEvaluator
	 * @param f file to be parsed for Rank values
	 * @return null if unable to extract information, complete rank otherwise 
	 */
	private static Rank processFile(File f) {
		try {
			System.out.println("Processing... " + f.getName());
			BufferedReader br = new BufferedReader(new FileReader(f));
			
			String line;
			Rank r = null;
			while ((line = br.readLine()) != null){
				// Try to reach the latest summary
				// This signifies we reach the valuable parts 
				if (line.contains(SUMMARY)){
					r = fillRank(br, f.getName());
				}
			}
			br.close();
			return r;
			
		} catch (FileNotFoundException e) {
			System.err.println("Unable to find file: " + f.getName());
		} catch (IOException e) {
			System.err.println("Error reading: " + f.getName());
		}
		return null;
	}

	/**
	 * Attempts to read in three values of rank
	 * @param br
	 * @param roFill
	 * @throws IOException 
	 */
	private static Rank fillRank(BufferedReader br, String name) throws IOException{
		
		boolean precFound = false, recallFound = false, fmeasFound = false;
		double prec = -1, recall = -1, fmeas = -1;
		
		for (int i = 0; i< 3; i++){
			String line = br.readLine();
			if (line == null) throw new IllegalArgumentException("Unable to extract all three entities");
			line = line.trim();
			String[] tokens = line.split(" ");
			if (tokens.length != 2)
				throw new IllegalArgumentException("Unable to parse: "+ tokens);
			if (tokens[0].equals(PRECISION)){
				prec = Double.parseDouble(tokens[1]);
				precFound = true;
			} 
			if (tokens[0].equals(RECALL)){
				recall = Double.parseDouble(tokens[1]);
				recallFound = true;
			}
			if (tokens[0].equals(F1)){
				fmeas = Double.parseDouble(tokens[1]);
				fmeasFound = true;
			}
		}
		
		if (precFound && recallFound && fmeasFound)
			return new Rank(name, prec, recall, fmeas);
		else return null;
	}
	
	private static void printUsage(String additional){
		System.out.print(USAGE + " " + additional);
	}
}

