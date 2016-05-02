package models;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import play.Logger;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;

public class SemanticSearchUtils {
	/**
	 * Returns w if w is a class or the name of the class that w is a synonymous with.
	 * If no match is found, null is returned.
	 * 
	 * @param w
	 * @return
	 */
	public static String try2Resolve2Class(String w){
		w = w.toLowerCase();
		
		if(w.equals("genre") || w.equals("genres"))
			return "Genre";
		if(w.equals("actor") || w.equals("actors"))
			return "Actor";
		if(w.equals("series") || w.equals("tvseries"))
			return "TVSeries";
		if(w.equals("char") || w.equals("character") || w.equals("characters"))
			return "Character";
		if(w.equals("year") || w.equals("years"))
			return "Year";
		if(w.equals("tag") || w.equals("tags"))
			return "Tag";
		if(w.equals("creator") || w.equals("creators"))
			return "Creator";
		
		return null;
	}
	
	public static String try2Resolve2Prop(String classWord, String propWord, String language){
		propWord = propWord.toLowerCase();
		
		if(language.equals("por")){
			if(propWord.equals("titulodeserie") || propWord.equals("titulo"))
				return "tituloDeSerie";
			if(propWord.equals("tempais") || propWord.equals("pais"))
				return "temPais";
			if(propWord.equals("temano") || propWord.equals("ano"))
				return "temAno";
			
			return null;
		}
		/*if(Utils.getMappingClassSynonymsAndProperties().get(classWord)!=null){
			//if((Utils.getMappingClassSynonymsAndProperties().get(classWord).get(propWord)!=null)){
			return Utils.getMappingClassSynonymsAndProperties().get(classWord).get(propWord);
			//}
		}*/
		
		Model model = TripleStoreManager.getInstance();
		Hashtable<String, String> connectingProperties = new Hashtable<String, String>();
		String q1 = "PREFIX foaf: <"+TSModel.PREFIX+"> PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?p WHERE { ?something foaf2:type foaf:"+classWord+" . ?something ?p ?o   }"; // . ?o foaf2:type foaf:"+assocClassName+"  UNION {?o foaf3:subClassOf foaf:"+assocClassName+"}
		//String q1 = "PREFIX foaf: <"+TSModel.PREFIX+"> PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?p WHERE { { {?o foaf2:type foaf:"+assocClassName+"} UNION {?o foaf3:subClassOf foaf:"+assocClassName+"} } . { {?series foaf2:type foaf:"+className+"} UNION {?series foaf3:subClassOf foaf:"+assocClassName+"} } . ?series ?p ?o   }"; // . ?o foaf2:type foaf:"+assocClassName+"  UNION {?o foaf3:subClassOf foaf:"+assocClassName+"}

		//String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?p WHERE { ?p foaf:hasTVSeriesActor foaf:"+assocClassName+" }";
		//System.out.println("q1 "+q1);
		//System.out.println("query "+q1);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		while(results.hasNext()){
			QuerySolution qs=results.next();
			//System.out.println("oleeee");
			//System.out.println("... "+qs.getResource("?p").toString());
			//System.out.println(".... "+qs));
			//connectingProperties.add(qs.getResource("?p").toString().replace(TSModel.PREFIX, ""));
			String myProperty = qs.getResource("?p").toString().replace(TSModel.PREFIX, "");
			String myPropertyLower = myProperty.toLowerCase();
			if(myPropertyLower.contains(propWord)){
				//System.out.println("done "+myProperty);
				return myProperty;
			}
			
		}
		
		/*if(classWord.equals("TVSeries")){
			if(propWord.equals("hastvseriestitle")){
				return "hasTVSeriesTitle";
				
			}
			if(propWord.equals("hascountry")){
				return "hasCountry";
				
			}
		}
		
		if(classWord.equals("Actor")){
			if(propWord.equals("hasname")){
				return "hasName";
				
			}
		}*/
		return null;
	}
	
	public static String try2Resolve2RangeProposition(String className, String str){
		if(className==null || str==null || !className.equals("TVSeries"))
			return null;
		
		str = str.toLowerCase().trim();
		if(str.equals("year"))
			return "hasYear";
		if(str.equals("rating"))
			return "hasRating";
		return null;
	}
	
	public static ArrayList<SearchResult> find(String instance){
		ArrayList<SearchResult> res = new ArrayList<SearchResult>();
		
		res.addAll(SemanticSearchUtils.find("Actor",instance));
		res.addAll(SemanticSearchUtils.find("Creator",instance));
		res.addAll(SemanticSearchUtils.find("Character",instance));
		res.addAll(SemanticSearchUtils.find("TVSeries",instance));
		
		return res;
	}

	public static ArrayList<SearchResult> find(String className, String instance, String assocClassName){
		Model model = TripleStoreManager.getInstance();
		ArrayList<SearchResult> arrayLSearchResult = new ArrayList<SearchResult>();
		ArrayList<SearchResult> arrayLSearchResultIntermedium = find(className, instance);
		//Hashtable<String, String> connectingProperties = allConnectingPropertiesBetweenTwoClasses(className, assocClassName);
		ArrayList<String> connectingProperties= Utils.getMappingClassClassConnectingProperty().get(className+assocClassName);
		if(connectingProperties==null){
			Logger.info("Connecting properties was zero. Go check it out because this should not have happened.");
			return arrayLSearchResult;
		}
		/*Enumeration en = connectingProperties.keys();
		while(en.hasMoreElements()){
			System.out.println("__---"+en.nextElement());
		}*/

		
		//System.out.println("size 1 "+arrayLSearchResultIntermedium.size());
		//System.out.println("size 2 "+connectingProperties.size());
		String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { ";
		if(arrayLSearchResultIntermedium.size()==0){
			Logger.info("No results found sorry");
			return arrayLSearchResult;
		}
		for(int i=0; i< arrayLSearchResultIntermedium.size(); i++){
			if(i>0) q1+= " UNION ";
			String phref = arrayLSearchResultIntermedium.get(i).getPageHref();
			phref = phref.replace("#", "");
			String idPhref = (String) phref.subSequence(phref.indexOf("id=")+3, phref.length());
			for(int j=0; j<connectingProperties.size(); j++){
				if(j>0) q1+= " UNION ";
				q1+=" { foaf:"+ idPhref +" foaf:"+connectingProperties.get(j) +" ?o } ";
				
			}
			
			
		}
		q1+=" } ";
		//System.out.println("my query "+q1);
		//q1 = "PREFIX foaf:   <http://www.semanticweb.org/ontologies/2011/9/WSTV.owl#> SELECT ?o WHERE {  foaf:tt0121955 foaf:hasTVSeriesActor ?o   } ";
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ArrayList<String> resultingResources = new ArrayList<String>();
		int resultCounter=0;
		Hashtable<String, String> resultingRes = new Hashtable<String, String>(); 
		while(results.hasNext()){
			QuerySolution qs=results.next();
			Resource anyResource = qs.getResource("?o");
			String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get(assocClassName))).getString();
			//System.out.println("result "+(++resultCounter)+" "+title);
			//arrayLSearchResult.add(new SearchResult("title?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			//resultingResources.add(qs.getResource("?s").toString());
			resultingRes.put(""+Utils.getMappingURLClass().get(assocClassName)+"?id="+anyResource.toString().replace(TSModel.PREFIX, ""), title);
		}
		Enumeration en = resultingRes.keys();
		while(en.hasMoreElements()){
			String keyElement = (String) en.nextElement();
			arrayLSearchResult.add(new SearchResult(keyElement,resultingRes.get(keyElement)));
		}
		
		/*String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?s WHERE { ?s foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?instanceName . FILTER regex(?tname, \""+instance+"\", \"i\" ) }";
		//System.out.println("query "+q1);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		ArrayList<String> resultingResources = new ArrayList<String>();
		while(results.hasNext()){
			QuerySolution qs=results.next();
			
			Resource anyResource = qs.getResource("?s");
			String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get("TVSeries"))).getString();
			
			//arrayLSearchResult.add(new SearchResult("title?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			resultingResources.add(qs.getResource("?s").toString());
		}*/
		System.out.println("q1 = "+q1);
		return arrayLSearchResult;
	}
	
	public static Hashtable<String, String> allConnectingPropertiesBetweenTwoClasses(String className, String assocClassName){
		Model model = TripleStoreManager.getInstance();
		Hashtable<String, String> connectingProperties = new Hashtable<String, String>();
		String q1 = "PREFIX foaf: <"+TSModel.PREFIX+"> PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?p ?o WHERE { ?o foaf2:type foaf:"+assocClassName+" . ?series foaf2:type foaf:"+className+" . ?series ?p ?o   }"; // . ?o foaf2:type foaf:"+assocClassName+"  UNION {?o foaf3:subClassOf foaf:"+assocClassName+"}
		//String q1 = "PREFIX foaf: <"+TSModel.PREFIX+"> PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?p WHERE { { {?o foaf2:type foaf:"+assocClassName+"} UNION {?o foaf3:subClassOf foaf:"+assocClassName+"} } . { {?series foaf2:type foaf:"+className+"} UNION {?series foaf3:subClassOf foaf:"+assocClassName+"} } . ?series ?p ?o   }"; // . ?o foaf2:type foaf:"+assocClassName+"  UNION {?o foaf3:subClassOf foaf:"+assocClassName+"}

		//String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?p WHERE { ?p foaf:hasTVSeriesActor foaf:"+assocClassName+" }";
		//System.out.println("q1 "+q1);
		//System.out.println("query "+q1);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		while(results.hasNext()){
			QuerySolution qs=results.next();
			//System.out.println("oleeee");
			//System.out.println("... "+qs.getResource("?p").toString());
			//System.out.println(".... "+qs));
			//connectingProperties.add(qs.getResource("?p").toString().replace(TSModel.PREFIX, ""));
			connectingProperties.put(qs.getResource("?p").toString().replace(TSModel.PREFIX, ""), "");
		}
		
		return connectingProperties;
		
	}
	
public static ArrayList<SearchResult> findByRange(String className, String proposition, double start, double end){
		
		ArrayList<SearchResult> arrayLSearchResult = new ArrayList<SearchResult>();
		Model model = TripleStoreManager.getInstance();
		
		
		
		//System.out.println("query "+q1);
		String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+">  SELECT ?s WHERE { { {?s foaf2:type foaf:"+className+"} UNION {?s foaf3:subClassOf foaf:"+className+"} } . ?s foaf:"+proposition+" ?year . FILTER (?year > "+start+") . FILTER (?year < "+end+")}";
		//System.out.println("query "+q1);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		while(results.hasNext()){
			QuerySolution qs=results.next();
			
			Resource anyResource = qs.getResource("?s");
			//System.out.println("anyResource "+anyResource.toString());
			String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get("TVSeries"))).getString();
			//System.out.println("title "+title);
			//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
			//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
			arrayLSearchResult.add(new SearchResult("title?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			
			//System.out.println("year "+ title);
		}
		return arrayLSearchResult;
	}
	
	/**
	 * Only exists for testing purposes
	 * @return
	 */
	public static ArrayList<SearchResult> findByRange(){
		
		ArrayList<SearchResult> arrayLSearchResult = new ArrayList<SearchResult>();
		Model model = TripleStoreManager.getInstance();
		
		
		
		//System.out.println("query "+q1);
		String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?s WHERE { ?s foaf:hasYear ?year . FILTER (?year > 2010)}";
		//System.out.println("query "+q1);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		while(results.hasNext()){
			QuerySolution qs=results.next();
			
			Resource anyResource = qs.getResource("?s");
			//System.out.println("anyResource "+anyResource.toString());
			String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get("TVSeries"))).getString();
			//System.out.println("title "+title);
			//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
			//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
			arrayLSearchResult.add(new SearchResult("title?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			
			//System.out.println("year "+ title);
		}
		return arrayLSearchResult;
	}
	
	public static ArrayList<SearchResult> findByProp(String className, String propName, String propValue, String language){
		Hashtable<String, String> resultingRes = new Hashtable<String, String>(); 
		Model model = TripleStoreManager.getInstance();
		ArrayList<SearchResult> arrayLSearchResult = new ArrayList<SearchResult>();
		String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+">  PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?s WHERE { { {?s foaf2:type foaf:"+className+"} UNION {?s foaf3:subClassOf foaf:"+className+"} }  . ?s foaf:"+propName+" ?o . FILTER regex(?o, \""+propValue+"\", \"i\" )}";
		if(language.equals("por")){
			q1 = "PREFIX foaf:   <"+TSModel.PREFIX+">  PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?s WHERE { { {?s foaf2:type foaf:"+className+"} UNION {?s foaf3:subClassOf foaf:"+className+"} }  . ?propo foaf3:comment \""+propName+"\"@pt . ?s ?propo ?o . FILTER regex(?o, \""+propValue+"\", \"i\" )}";
			
		}
		//String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+">  PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?s WHERE { ?s foaf2:type foaf:"+className+"  . ?s foaf:"+propName+" ?o . FILTER regex(?o, \""+propValue+"\", \"i\" )}";
		//System.out.println("query "+q1);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		
		while(results.hasNext()){
			//System.out.println(".....");
			QuerySolution qs=results.next();
			
			Resource anyResource = qs.getResource("?s");
			//System.out.println("anyResource "+anyResource.toString());
			String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get(className))).getString();
			//System.out.println("title "+title);
			//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
			//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
			//arrayLSearchResult.add(new SearchResult(""+Utils.getMappingURLClass().get(className)+"?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			resultingRes.put(""+Utils.getMappingURLClass().get(className)+"?id="+anyResource.toString().replace(TSModel.PREFIX, ""), title);
		}
		
		Enumeration en = resultingRes.keys();
		while(en.hasMoreElements()){
			String keyElement = (String) en.nextElement();
			arrayLSearchResult.add(new SearchResult(keyElement,resultingRes.get(keyElement)));
		}
		return arrayLSearchResult;
	}
	
	public static ArrayList<SearchResult> findByPropPT(String className, String propName, String propValue){
		Hashtable<String, String> resultingRes = new Hashtable<String, String>(); 
		Model model = TripleStoreManager.getInstance();
		ArrayList<SearchResult> arrayLSearchResult = new ArrayList<SearchResult>();
		String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+">  PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?s WHERE { { {?s foaf2:type foaf:"+className+"} UNION {?s foaf3:subClassOf foaf:"+className+"} }  . ?propo foaf3:comment \""+propName+"\"@pt . ?s ?propo ?o . FILTER regex(?o, \""+propValue+"\", \"i\" )}";
		//String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+">  PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> PREFIX foaf3: <"+TSModel.SUBCLASSPREFIX+"> SELECT ?s WHERE { ?s foaf2:type foaf:"+className+"  . ?s foaf:"+propName+" ?o . FILTER regex(?o, \""+propValue+"\", \"i\" )}";
		//System.out.println("query languages "+q1);
		com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect();
		
		while(results.hasNext()){
			//System.out.println(".....");
			QuerySolution qs=results.next();
			
			Resource anyResource = qs.getResource("?s");
			//System.out.println("anyResource "+anyResource.toString());
			String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get(className))).getString();
			//System.out.println("title "+title);
			//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
			//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
			//arrayLSearchResult.add(new SearchResult(""+Utils.getMappingURLClass().get(className)+"?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			resultingRes.put(""+Utils.getMappingURLClass().get(className)+"?id="+anyResource.toString().replace(TSModel.PREFIX, ""), title);
		}
		
		Enumeration en = resultingRes.keys();
		while(en.hasMoreElements()){
			String keyElement = (String) en.nextElement();
			arrayLSearchResult.add(new SearchResult(keyElement,resultingRes.get(keyElement)));
		}
		return arrayLSearchResult;
	}
	
	public static ArrayList<SearchResult> find(String className, String instance){
		//System.out.println("NICE FIND className#"+className+"##"+instance+"#");
		Model model = TripleStoreManager.getInstance();
		ArrayList<SearchResult> arrayLSearchResult = new ArrayList<SearchResult>();
		
		if(className.equals("Genre")){
			
			String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?s WHERE { ?s foaf:hasGenre foaf:"+instance+"}";
			//System.out.println("query "+q1);
			com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution qs=results.next();
				
				Resource anyResource = qs.getResource("?s");
				//System.out.println("anyResource "+anyResource.toString());
				String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get("TVSeries"))).getString();
				//System.out.println("title "+title);
				//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
				//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
				arrayLSearchResult.add(new SearchResult("title?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			}
		} else if(className.equals("Year")){
			//String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?s WHERE { ?s foaf:hasYear ?year . FILTER regex(?year, \""+instance+"\", \"i\" )}";
			//. FILTER (?year > 2010)
			String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?s WHERE { ?s foaf:hasYear ?year . FILTER (?year = "+instance+")}";
			//System.out.println("query "+q1);
			com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution qs=results.next();
				
				Resource anyResource = qs.getResource("?s");
				//System.out.println("anyResource "+anyResource.toString());
				String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get("TVSeries"))).getString();
				//System.out.println("title "+title);
				//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
				//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
				arrayLSearchResult.add(new SearchResult("title?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			}
		}  else if(className.equals("Tag") && (instance.equals("")==false)){
			String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?s WHERE { ?s foaf:hasTag ?o . ?o foaf:hasTagName ?tname . FILTER regex(?tname, \""+instance+"\", \"i\" )}";
			//System.out.println("query "+q1);
			com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution qs=results.next();
				
				Resource anyResource = qs.getResource("?s");
				//System.out.println("anyResource "+anyResource.toString());
				String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get("TVSeries"))).getString();
				//System.out.println("title "+title);
				//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
				//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
				arrayLSearchResult.add(new SearchResult("title?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			}
		}else if (className.equals("Tag") && (instance.equals("")==true)){
			String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> PREFIX foaf2: <"+TSModel.TYPEPREFIX+">  SELECT ?o WHERE { ?s foaf2:type foaf:Tag . ?s foaf:hasTagName ?o}";
			//System.out.println("query "+q1);
			com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution qs=results.next();
				
				//Resource anyResource = qs.getResource("?o");
				String tagName = qs.getLiteral("?o").toString();
				//System.out.println("anyResource "+anyResource.toString());
				//String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get("TVSeries"))).getString();
				//System.out.println("title "+title);
				//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
				//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
				arrayLSearchResult.add(new SearchResult(("find?q=tag+"+tagName),tagName));
			}
		} else {
			String q1 = "PREFIX foaf:   <"+TSModel.PREFIX+"> PREFIX foaf2: <"+TSModel.TYPEPREFIX+"> SELECT ?s WHERE { ?s foaf2:type foaf:"+className+" . ?s foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?instanceName . FILTER regex(?instanceName, \""+instance+"\", \"i\" ) }";
			System.out.println("MINHA query "+q1);
			com.hp.hpl.jena.query.Query query = QueryFactory.create(q1);
			QueryExecution qexec = QueryExecutionFactory.create(query, model);
			ResultSet results = qexec.execSelect();
			while(results.hasNext()){
				QuerySolution qs=results.next();
				
				Resource anyResource = qs.getResource("?s");
				//System.out.println("anyResource "+anyResource.toString());
				String title = anyResource.getProperty(model.getProperty(TSModel.PREFIX+Utils.getMappingClassNameProperty().get(className))).getString();
				//System.out.println("title "+title);
				//String q3 = "PREFIX foaf:   <"+TSModel.PREFIX+"> SELECT ?o WHERE { foaf:"+anyResource.toString().replace(TSModel.PREFIX, "")+" foaf:"+Utils.getMappingClassNameProperty().get(className)+" ?o}";
				//res.add(genreResource.toString().replace(TSModel.PREFIX, ""));
				arrayLSearchResult.add(new SearchResult(""+Utils.getMappingURLClass().get(className)+"?id="+anyResource.toString().replace(TSModel.PREFIX, ""),title));
			}
		}
		
		return arrayLSearchResult;
	}
}

