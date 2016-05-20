package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import play.cache.Cache;

public class Recomendations {
	public static void seenThis(String sessionId, Series s){
		ArrayList<Series> messages = Cache.get(sessionId + "-messages", ArrayList.class);
	    if(messages == null) {
	        // Cache miss
	    	messages = new ArrayList<Series>();
	    }
	    
	    messages.add(s);
	    
	    //Ensures the list does not grow too big
	    //ArrayList is probably not the adequate data structure for this, oh well
    	while(messages.size() > 50)
    		messages.remove(0);
	    
	    Cache.set(sessionId + "-messages", messages, "30mn");
	}
	
	public static void clearHistory(String sessionId){
		ArrayList<Series> messages = Cache.get(sessionId + "-messages", ArrayList.class);
	    if(messages != null) {
	    	Cache.set(sessionId + "-messages", new ArrayList<Series>(), "30mn");
	    }
	}
	
	public static ArrayList<Series> getThem(String sessionId, int n){
		ArrayList<Series> res = getNRecomended2User(sessionId, n, "");
		if(res.size() > 0)
			return res;
		else
			return Series.getNRecomended4Anyone(n, "");
	}
	
	public static ArrayList<Series> getThem(String sessionId){
		ArrayList<Series> res = getNRecomended2User(sessionId, 5, "");
		if(res.size() > 0)
			return res;
		else
			return Series.getNRecomended4Anyone(5, "");
	}
	
	public static ArrayList<Series> getThem(String sessionId, String excludeThisId){
		ArrayList<Series> res = getNRecomended2User(sessionId, 5, excludeThisId);
		if(res.size() > 0)
			return res;
		else
			return Series.getNRecomended4Anyone(5, excludeThisId);
	}
	
	public static ArrayList<Series> getThem(String sessionId,int n, String excludeThisId){
		ArrayList<Series> res = getNRecomended2User(sessionId, n, excludeThisId);
		if(res.size() > 0)
			return res;
		else
			return Series.getNRecomended4Anyone(n, excludeThisId);
	}
	
	
	private static ArrayList<Series> getNRecomended2User(String sessionId, int n, String excludeThisId){
		if(n == 0){
			return new ArrayList<Series>();
		}
		
		ArrayList<Series> lastSeen = getLastSeenSeries(sessionId);
		
		if(lastSeen.size()==0){
			return new ArrayList<Series>();
		}
		
		ArrayList<Series> res = getRecomendedByRanking(lastSeen, excludeThisId);
		
		//Only take n series id n > 0
		if(n > 0){
			n = n > res.size() ? res.size() : n;
			return new ArrayList<Series>(res.subList(0, n));
		}
		else
			return res;
	}
	
	private static ArrayList<Series> getRecomendedByRanking(ArrayList<Series> lastSeen, String excludeThisId){
		ArrayList<Series> res = new ArrayList<Series>();
		ArrayList<Pair<Series, Double>> rank = getRecomendedByRankingWithScore(lastSeen, excludeThisId);
		
		for(Pair<Series, Double> pair : rank){
			res.add(pair.t);
		}
		
		return res;
	}
	
	private static ArrayList<Pair<Series, Double>> getRecomendedByRankingWithScore(ArrayList<Series> lastSeen, String excludeThisId){
		Hashtable<String, Integer> genreHist = new Hashtable<String, Integer>();
		Hashtable<String, Integer> creatorHist = new Hashtable<String, Integer>();
		
		//Build an histogram by genre, don't filter repetitions
		for(Series sr : lastSeen){
			for(String genre : sr.getGenres()){
				int nOcurrs = 1;
				if(genreHist.containsKey(genre)){
					nOcurrs = genreHist.get(genre) + 1;
				}
				
				genreHist.put(genre, nOcurrs);
			}
		}
		
		//Build an histogram by creator, don't filter repetitions
		for(Series sr : lastSeen){
			for(Person cretor : sr.getCreators()){
				int nOcurrs = 1;
				String pid = cretor.getId();
				
				if(creatorHist.containsKey(pid)){
					nOcurrs = creatorHist.get(pid) + 1;
				}
				
				creatorHist.put(pid, nOcurrs);
			}
		}
		
		//Build the ranking
		ArrayList<Series> allS = getAllSeries();
		ArrayList<Pair<Series, Double>> rank = new ArrayList<Pair<Series,Double>>();
		for(Series sr : allS){
			double ts = 1;
			for(String genre : sr.getGenres()){
				if(genreHist.containsKey(genre))
					ts += genreHist.get(genre);
			}
			
			for(Person creator : sr.getCreators()){
				String pid = creator.getId();
				if(creatorHist.containsKey(pid)){
					ts += creatorHist.get(pid);
				}
			}
			
			String rating = sr.getRating();
			rating = rating.equals("NOTFOUND") ? "5" : rating;
			ts *= Double.parseDouble(rating) / 10;
			
			rank.add(new Pair<Series, Double>(sr, ts));
		}
		
		//Must exclude lastSeen and excludeThisId
		for(int i=0; i < rank.size();i++){
			String id = rank.get(i).t.getId();
			if(excludeThisId.equals(id) || lastSeen.contains(rank.get(i).t)){
				rank.remove(i);
				i--;
				continue;
			}
		}
		
		//Sort it and retrieve the result in order
		Collections.sort(rank);
		Collections.reverse(rank);
		
		return rank;
	}
	
	public static ArrayList<Series> getAllSeries(){
		ArrayList<Series> messages = Cache.get("allSeries", ArrayList.class);
	    if(messages == null) {
	        // Cache miss
	    	messages = new ArrayList<Series>();
	    	
	    	//Get series
	    	for(String id : Series.getAll())
	    		messages.add(Series.get(id));
	    	
		    Cache.set("allSeries", messages, "30mn");
	    }
	    
	    return messages;
	}
	
	private static ArrayList<Series> getRecomendedByYear(ArrayList<Series> lastSeen, String excludeThisId){
		Hashtable<String, Integer> hist = new Hashtable<String, Integer>();
		
		//Build an histogram, don't filter repetitions
		for(Series sr : lastSeen){
			int nOcurrs = 1;
			if(sr.getYear().equals("NOTDEFINED"))
				continue;
			
			if(hist.containsKey(sr.getYear())){
				nOcurrs = hist.get(sr.getYear()) + 1;
			}
			
			hist.put(sr.getYear(), nOcurrs);
		}
		
		//Convert to to ArrayList for sorting
		ArrayList<Pair<String, Integer>> histList = new ArrayList<Pair<String,Integer>>();
		for(String key : hist.keySet()){
			histList.add(new Pair<String, Integer>(key, hist.get(key)));
		}
		
		if(histList.size()==0)
			return new ArrayList<Series>();
		
		//Sort - because Pair implements compareTo on the value
		//And put the keys with most hits on the start
		Collections.sort(histList);
		Collections.reverse(histList);
		
		//For now uses the semantic search to fetch the results
		String query = "Year "+histList.get(0).t;
		ArrayList<SearchResult> tRes = (new SemanticSearch(query, "eng")).execute();
		ArrayList<Series> res = new ArrayList<Series>();
		for(SearchResult sr : tRes){
			String id = sr.getPageHref().split("=")[1];
			res.add(Series.get(id));
		}
		
		//Must exclude lastSeen and excludeThisId
		for(int i=0; i < res.size();i++){
			String id = res.get(i).getId();
			if(excludeThisId.equals(id) || lastSeen.contains(res.get(i))){
				res.remove(i);
				i--;
				continue;
			}
		}
		
		return res;
	}
	
	private static ArrayList<Series> getRecomendedByGenre(ArrayList<Series> lastSeen, String excludeThisId){
		Hashtable<String, Integer> genreHist = new Hashtable<String, Integer>();
		
		//Build an histogram by genre, don't filter repetitions
		for(Series sr : lastSeen){
			for(String genre : sr.getGenres()){
				int nOcurrs = 1;
				if(genreHist.containsKey(genre)){
					nOcurrs = genreHist.get(genre) + 1;
				}
				
				genreHist.put(genre, nOcurrs);
			}
		}
		
		//Convert to to ArrayList for sorting
		ArrayList<Pair<String, Integer>> genreHistList = new ArrayList<Pair<String,Integer>>();
		for(String genre : genreHist.keySet()){
			genreHistList.add(new Pair<String, Integer>(genre, genreHist.get(genre)));
		}
		
		//Sort - because Pair implements compareTo on the value
		//And put the genres with most hits on the start
		Collections.sort(genreHistList);
		Collections.reverse(genreHistList);
		
		//For now uses the semantic search to fetch the results
		String query = "Genre ";
		for(int i=0; i < 2 && i < genreHistList.size();i++){
			if(i > 0)
				query += " and ";
			query += genreHistList.get(i).t;
		}
		query = query.trim();

		ArrayList<SearchResult> tRes = (new SemanticSearch(query, "eng")).execute();
		ArrayList<Series> res = new ArrayList<Series>();
		for(SearchResult sr : tRes){
			String id = sr.getPageHref().split("=")[1];
			res.add(Series.get(id));
		}
		
		//Must exclude lastSeen and excludeThisId
		for(int i=0; i < res.size();i++){
			String id = res.get(i).getId();
			if(excludeThisId.equals(id) || lastSeen.contains(res.get(i))){
				res.remove(i);
				i--;
				continue;
			}
		}
		
		return res;
	}
	
	
	private static ArrayList<Series> getLastSeenSeries(String sessionId){
		ArrayList<Series> messages = Cache.get(sessionId + "-messages", ArrayList.class);
	    if(messages == null) {
	        // Cache miss
	    	messages = new ArrayList<Series>();
	        Cache.set(sessionId + "-messages", messages, "30mn");
	    }
	    
	    return messages;
	}
	
	public static ArrayList<Pair<Series, Double>> orderByTarget(Series target){
		ArrayList<Series> targetAL = new ArrayList<Series>();
		targetAL.add(target);
		
		return getRecomendedByRankingWithScore(targetAL, "");
	}
}

