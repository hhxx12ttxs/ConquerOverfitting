package uk.ac.mimas.names.disambiguator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import org.apache.log4j.Logger;


import uk.ac.mimas.names.disambiguator.types.*;
import uk.ac.mimas.names.disambiguator.util.StringUtil;
import uk.ac.mimas.names.disambiguator.util.textanalyser.TextAnalyser;


/**
 * 
* This is the main entry class for the name-disambiguator, which provides
* functionality for normalising and disambiguating multiple source records
* 
* @author Dan Needham <daniel.needham@manchester.ac.uk>
* 
*/

public class NamesDisambiguator {
	private static final Logger logger = Logger.getLogger(NamesDisambiguator.class.getName());
	private Map<String, Integer> weightings;
	
	// Default weighting keys
	public static final String NAME_WEIGHT_KEY = "NAME_WEIGHT";
	public static final String TITLE_WEIGHT_KEY = "TITLE_WEIGHT";
	public static final String FIELD_OF_ACTIVITY_WEIGHT_KEY = "FIELD_OF_ACTIVITY_WEIGHT";
	public static final String AFFILIATION_WEIGHT_KEY = "AFFILIATION_WEIGHT";
	public static final String RESULT_PUBLICATION_TITLE_WEIGHT_KEY = "RESULT_PUBLICATION_TITLE_WEIGHT";
	public static final String RESULT_PUBLICATION_WORDMAP_WEIGHT_KEY = "RESULT_PUBLICATION_WORDMAP_WEIGHT";
	public static final String IDENTIFIER_WEIGHT_KEY = "IDENTIFIER_WEIGHT";
	public static final String COLLABORATION_WEIGHT_KEY = "COLLABORATION_WEIGHT";
	
	// Default weightings values
	private static final int NAME_DEFAULT_WEIGHT = 2;
	private static final int TITLE_DEFAULT_WEIGHT = 2;
	private static final int FIELD_OF_ACTIVITY_DEFAULT_WEIGHT = 5;
	private static final int AFFILIATION_DEFAULT_WEIGHT = 5;
	private static final int RESULT_PUBLICATION_TITLE_DEFAULT_WEIGHT = 30;
	private static final int RESULT_PUBLICATION_WORDMAP_DEFAULT_WEIGHT = 2;
	private static final int IDENTIFIER_DEFAULT_WEIGHT = 100; 		
	private static final int COLLABORATION_DEFAULT_WEIGHT = 5;		
	
	/**
	 * Constructor
	 */
	public NamesDisambiguator(){
		
		this.setDefaultWeightings();
	
	}
	/**
	 * Constructor
	 * @param w A map of custom weighting scores
	 */
	public NamesDisambiguator(Map<String, Integer> w){
		this.setDefaultWeightings();
		weightings.putAll(w);
	}
	
	public void setDefaultWeightings(){
		weightings = new HashMap<String, Integer>();
		weightings.put(NAME_WEIGHT_KEY, NAME_DEFAULT_WEIGHT);
		weightings.put(TITLE_WEIGHT_KEY, TITLE_DEFAULT_WEIGHT);
		weightings.put(FIELD_OF_ACTIVITY_WEIGHT_KEY, FIELD_OF_ACTIVITY_DEFAULT_WEIGHT );
		weightings.put(AFFILIATION_WEIGHT_KEY, AFFILIATION_DEFAULT_WEIGHT);
		weightings.put(RESULT_PUBLICATION_TITLE_WEIGHT_KEY, RESULT_PUBLICATION_TITLE_DEFAULT_WEIGHT);
		weightings.put(RESULT_PUBLICATION_WORDMAP_WEIGHT_KEY, RESULT_PUBLICATION_WORDMAP_DEFAULT_WEIGHT);
		weightings.put(IDENTIFIER_WEIGHT_KEY, IDENTIFIER_DEFAULT_WEIGHT);
		weightings.put(COLLABORATION_WEIGHT_KEY, COLLABORATION_DEFAULT_WEIGHT);
	}
	
	/**
     * Takes a list of normalised records and compares them against each other
     * for matchedness. Each source record contains a list of amtch scores
     * against the other records in the list
     * 
     * @param source An list of normalised records to me disambiguated
     * @return Nothing.
     */
	
	public void matchCollection(ArrayList<NormalisedRecord> source){
		for(NormalisedRecord n: source){
			for(NormalisedRecord c: source){
				if(n==c) 															// don't compare the same record.
					continue;
				if(n.getMatchResults().containsKey(c)) 								// don't match if a score is already present.
					continue;
				double score = match(n,c);
				n.getMatchResults().put(c, score);									// Set the match score in both records match list.
				c.getMatchResults().put(n, score);
			}
		}
	}
	
	
	/**
	 * 
	 *  Takes a list of normalised records and checks 
	 * 
	 * @param toMerge
	 * @param threshold
	 * @return
	 */
	public ArrayList<NormalisedRecord> mergeCollection(ArrayList<NormalisedRecord> toMerge, double threshold){
		ArrayList<NormalisedRecord> mergedRecords = new ArrayList<NormalisedRecord>();
		// To avoid matching records twice they are added to ignore list after being matched. This is checked each iteration.
		ArrayList<NormalisedRecord> ignoreRecords = new ArrayList<NormalisedRecord>();
		
		for(NormalisedRecord root: toMerge){
			   if(ignoreRecords.contains(root))										// Ignore anything that has already been matched.
				   continue;
			   mergedRecords.add(root);
			   Map<NormalisedRecord, Double> map =  root.getMatchResults();			// check each of the match result entries.
			   for (Map.Entry<NormalisedRecord, Double> entry : map.entrySet()) {
					if(entry.getValue().doubleValue() >= threshold){
						root.mergeWith(entry.getKey(), entry.getValue());			// merge anything with a score above the threshold.
						ignoreRecords.add(entry.getKey());							// add the mergee to the ignore list.
					}
				}
			  
			   
		}	
		return mergedRecords;
	}
	
	
	/**
     * Attempts to derive a match score by comparing two normalised records.
     * 
     * @param sourceRecord The first record 
     * @param comparisonRecord A record to compare against
     * @return A match score between 0.0 and 100.00
     */
	
	public double match(NormalisedRecord sourceRecord, NormalisedRecord comparisonRecord){
		double score = 0.0;
		try{
			
			// If the objects are the same then return a direct match
			if(this.equals(comparisonRecord))
				return 100.00;
			
			// Compare Names
			
			// first do an exact string match
			
			// if it fails, do a deeper match
			
			ArrayList<double[]> nameMatches = new ArrayList<double[]>();
			for(NormalisedName currentName : sourceRecord.getNormalisedNames()){
				for(NormalisedName matchName : comparisonRecord.getNormalisedNames()){
				  nameMatches.add(currentName.compare(matchName));									// analyse how well each name matches.
				}
			}
			
			double[] avgNameMatch = new double[2];
			double familyNameTotal, givenNameTotal;
			familyNameTotal = givenNameTotal = 0;
			avgNameMatch[0] = avgNameMatch[1] = 0;
			for(double[] match : nameMatches){
				familyNameTotal += match[0];
				givenNameTotal += match[1];
			}
			
			avgNameMatch[0] = (familyNameTotal / (nameMatches.size() * 100)) * 100;					// get an average score for family names and given names.
			avgNameMatch[1] = (givenNameTotal / (nameMatches.size() * 100)) * 100;
			
			score = (((avgNameMatch[0] + avgNameMatch[1]) / 200) * 100) / weightings.get(NAME_WEIGHT_KEY);	 					// max overall score from name match is 50%.
			
		
			// Compare Identifiers
			for(NormalisedIdentifier thisIdentifier : sourceRecord.getNormalisedIdentifiers()){
				for(NormalisedIdentifier matchIdentifier : comparisonRecord.getNormalisedIdentifiers()){
					if(thisIdentifier.getIdentifier().equalsIgnoreCase(matchIdentifier.getIdentifier())
						&& thisIdentifier.getBasisFor().equalsIgnoreCase(matchIdentifier.getBasisFor())){
						score+= weightings.get(IDENTIFIER_WEIGHT_KEY);
					}
				}
			}
			
			// Compare Salutations

			for(NormalisedTitle thisTitle : sourceRecord.getNormalisedTitles()){
				for(NormalisedTitle matchTitle : comparisonRecord.getNormalisedTitles()){
					if(thisTitle.getTitle().equalsIgnoreCase(matchTitle.getTitle())){				// Simple string match on title.
						score += weightings.get(TITLE_WEIGHT_KEY);
					}
				}
			}
			
			// Compare Fields of Activity
			
			for(NormalisedFieldOfActivity thisFieldOfActivity : sourceRecord.getNormalisedFieldsOfActivity()){
				for(NormalisedFieldOfActivity matchFieldOfActivity : comparisonRecord.getNormalisedFieldsOfActivity()){
					if(thisFieldOfActivity.getFieldOfActivity().equalsIgnoreCase(matchFieldOfActivity.getFieldOfActivity())){	// Simple string match on field of activity.
						score += weightings.get(FIELD_OF_ACTIVITY_WEIGHT_KEY);
					}
				}
			}
			
			// Compare Affiliation
			
			for(NormalisedAffiliation thisAffiliation : sourceRecord.getNormalisedAffiliations()){
				for(NormalisedAffiliation matchAffiliation : comparisonRecord.getNormalisedAffiliations()){
					if(thisAffiliation.match(matchAffiliation) >= 50){								// get a match score for each affiliation (threshold currently set at 75%)
						score += weightings.get(AFFILIATION_WEIGHT_KEY);
					}
				}
			}
				
			// Compare result publications
			
			
			int l = 0;
			for(NormalisedResultPublication thisResultPublication : sourceRecord.getNormalisedResultPublications()){
				String thisResultPublicationChars = StringUtil.getCharactersOnly(thisResultPublication.getTitle());
				TextAnalyser thisResultPublicationAnalyser = new TextAnalyser(4);
				thisResultPublicationAnalyser.processString(thisResultPublication.getTitle());
				for(NormalisedResultPublication matchResultPublication : comparisonRecord.getNormalisedResultPublications()){
					
					if(thisResultPublicationChars.equalsIgnoreCase(StringUtil.getCharactersOnly(matchResultPublication.getTitle()))){		// Is the title an exact match?
						score += weightings.get(RESULT_PUBLICATION_TITLE_WEIGHT_KEY);
						continue;
					}
					
					
					TextAnalyser matchResultPublicationAnalyser = new TextAnalyser(4);
					matchResultPublicationAnalyser.processString(matchResultPublication.getTitle());
					l = TextAnalyser.compareWordMaps(thisResultPublicationAnalyser.getWordMap(), matchResultPublicationAnalyser.getWordMap());	// Use text analyser to compare titles.
					score += l * (l/weightings.get(RESULT_PUBLICATION_WORDMAP_WEIGHT_KEY));
				}
			}
			
			// Compare Collaboration
			
			for(NormalisedCollaboration thisCollaboration : sourceRecord.getNormalisedCollaborations()){
				for(NormalisedCollaboration matchCollaboration : comparisonRecord.getNormalisedCollaborations()){
					if(thisCollaboration.match(matchCollaboration) >= 75){								// get a match score for each affiliation (threshold currently set at 75%)
						score += weightings.get(COLLABORATION_WEIGHT_KEY);
					}
				}
			}
			
		}
		catch(Exception e){
			logger.error("Error comparing records: " + e.toString());
			
			e.printStackTrace();
		}
		return score > 100.00 ? 100 : score;
	}
}

