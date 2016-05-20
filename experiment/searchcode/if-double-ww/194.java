/**
 * 
 */
package it.uniroma1.di.livi;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ReturnableEvaluator;
import org.neo4j.graphdb.StopEvaluator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.TraversalPosition;
import org.neo4j.graphdb.Traverser;
import org.neo4j.graphdb.Traverser.Order;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.WordnetStemmer;

import it.uniroma1.di.livi.configuration.Configurator;
import it.uniroma1.di.livi.configuration.Constants;
import it.uniroma1.di.livi.contextgraph.ContextGraphGenerator;
import it.uniroma1.di.livi.data.SensesIntegrator;
import it.uniroma1.di.livi.data.Word;
import it.uniroma1.lcl.jlt.Configuration;
import it.uniroma1.lcl.jlt.Constants.GraphEdgeLabels;
import it.uniroma1.lcl.jlt.Constants.GraphNodeProperties;
import it.uniroma1.lcl.jlt.graph.neo4j.Neo4jGraph;
import it.uniroma1.lcl.jlt.graph.neo4j.analysis.Neo4jGraphAnalyzer;
import it.uniroma1.lcl.jlt.graph.neo4j.analysis.centrality.CentralityManager;
import it.uniroma1.lcl.jlt.graph.neo4j.generation.ts.Neo4jGraphReader;
import it.uniroma1.lcl.jlt.graph.neo4j.generation.ts.Neo4jGraphWriter;
import it.uniroma1.lcl.jlt.graph.neo4j.routines.TSCommonRoutines;
import it.uniroma1.lcl.jlt.util.Language;
import it.uniroma1.lcl.jlt.util.Stopwords;
import it.uniroma1.lcl.jlt.wiki.SearchWiki;
import it.uniroma1.lcl.jlt.wiki.SearchWikiCentral;
import it.uniroma1.lcl.jlt.wiki.WikiPage;
import it.uniroma1.lcl.jlt.wiki.WikiText;
import it.uniroma1.lcl.jlt.wiki.WikiWord;
import it.uniroma1.lcl.jlt.wordnet.WordNetVersion;

/**
 * Common routines
 * @author strozzino
 *
 */
public class Routines {

	/**
	 * Local configurations
	 */
	private Configurator config=Configurator.getInstance();
	
	/**
	 * JLT configurator
	 */
	private Configuration jltConfig=Configuration.getInstance();
	
	/**
	 * Neo4j graph analyzer
	 */
	private Neo4jGraphAnalyzer analyzer=null;
	
	/**
	 * Internal neo4j traverser
	 */
	private Traverser traverser=null;
	
	/**
	 * Max depth for the visit
	 */
	private int MAX_DEPTH=0;
	
	/**
	 * Wordnet dictionary
	 */
	private Dictionary wnDictionary=null;
	
	/**
	 * Wordnet stemmer
	 */
	private WordnetStemmer wnStemmer=null;
	
	/**
	 * Search wiki instance
	 */
	private SearchWiki sw=null;
	
	/**
	 * Transaction-safe Common routines 
	 */
	private TSCommonRoutines tsRoutines=null;
	
	/**
	 * Stopwords utility
	 */
	private Stopwords stopWords=null;
	
	/**
	 * An array of context words
	 */
	private ArrayList<Word> cws=null;
	
	/**
	 * EigenvectorCentralityManager
	 */
	private CentralityManager manager=null;
	
	
	/**
	 * Empty constructor
	 */
	public Routines()
	{
		this.analyzer=new Neo4jGraphAnalyzer();
		this.MAX_DEPTH=Integer.parseInt(config.getProperties(Constants.CONF_KEY_VISIT_MAX_DEPTH));
	}
	
	/**
	 * Constructor
	 * @param maxDepth
	 */
	public Routines(int maxDepth)
	{
		this.analyzer=new Neo4jGraphAnalyzer();
		this.MAX_DEPTH=maxDepth;
	}
	
	/**
	 * Constructor
	 * @param a
	 * @param maxDepth
	 */
	public Routines(Neo4jGraphAnalyzer a, int maxDepth)
	{
		this.analyzer=a;
		this.MAX_DEPTH=maxDepth;
	}
	
	/**
	 * Main open method.
	 * @throws Exception
	 */
	public void open() throws Exception
	{
		//neo4j utility
		tsRoutines=analyzer.getTSCommonRoutines();
		analyzer.openGraph();
		//wordnet utlity
		wnDictionary=new Dictionary(new URL("file", null, jltConfig.getWordNetData(WordNetVersion.fromString("3.0"))));
		wnDictionary.open();
		wnStemmer=new WordnetStemmer(wnDictionary);
		//searchWiki utlity
		sw=SearchWikiCentral.getInstance(Language.EN);
		//stopwords utility
		stopWords=Stopwords.getInstance();
		stopWords.load(jltConfig.getStopwordsFile());
	}
	
	/**
	 * Main close method.
	 * @throws Exception
	 */
	public void close() throws Exception
	{
		if(analyzer!=null)
			analyzer.closeGraph();
		if(wnDictionary!=null)
			wnDictionary.close();
		if(sw!=null)
			sw.close();
		stopWords=null;
	}
	
	/**
	 * Executes a DFSAlgorithm starting from a specified node.
	 * The DFSAlgorithm is limited at a MAX_DEPTH depth.
	 * @param startNode
	 * @return the traverser object representing the visit
	 */
	public Traverser executeNeo4jDFS(Node startNode)
	{
		traverser=startNode.traverse(Order.DEPTH_FIRST, 
    				new StopEvaluator() {
						public boolean isStopNode(TraversalPosition arg0) {
							if(arg0.depth()>=MAX_DEPTH)
								return true;
							else
								return false;
						}
					}, ReturnableEvaluator.ALL, GraphEdgeLabels.SEMANTICS, Direction.OUTGOING, GraphEdgeLabels.REDIRECTION, Direction.OUTGOING);
		
		return traverser;
	}
	
	/**
	 * Extracts the senses from wikipedia lucene index.
	 * @param lemma
	 * @return an hash set of wiki sense of this lemma
	 * @throws Exception
	 */
	public HashSet<String> getWikiSenses(String lemma) throws Exception
	{
		Set<String> wikiSense=sw.getSenseTitles(lemma);
		HashSet<String> set=new HashSet<String>(wikiSense.size());
		for(String sense: wikiSense)
		{
			if(!sense.contains("List of")&&!sense.contains("list of"))
				set.add(sense);
		}
		
		SensesIntegrator.integrate(set, lemma);
		
		return set;
	}
	
	/**
	 * Tokenize a given text preserving the target word.
	 * @param text The text to tokenize
	 * @param targetWord The target word. Can be null
	 * @return an array of token
	 */
	private ArrayList<String> getTokenizedText(String text, String targetWord)
	{
		ArrayList<String> array=new ArrayList<String>();
		
		//normalizes text to lower case
		text=text.toLowerCase();
		if(targetWord!=null)
			targetWord=targetWord.toLowerCase();
		
		//if the targetWord is of the form "word1 word2 ... wordn"
		if(targetWord!=null&&targetWord.contains(" "))
		{
			String old=targetWord;
			targetWord=targetWord.replace(" ", "_");
			text=text.replace(old, targetWord);
		}
		
		//for each word
		for(String w: text.split("[( \\\\.:,;\"'!\\\\?)]"))
		{
			w=w.trim();
			String[] arrayTmp=null;
			if(targetWord!=null&&w.equalsIgnoreCase(targetWord))
			{
				arrayTmp=new String[1];
				arrayTmp[0]=w.replace("_", " ");
			}
			else
				arrayTmp=w.split("-");
			
			for(String token: arrayTmp)
			{
				if(!token.isEmpty())
					array.add(token);
			}
		}
		return array;
	}
	
	/**
	 * Finds the lemma of a given word using wordnet dictionary.
	 * @param word
	 * @return the lemma of the word or null if it doesn't exists
	 */
	public String getLemma(String word)
	{
		return getStem(word, 0);
	}
	
	/**
	 * Finds the stem, at a specific position, of a given word.
	 * @param word
	 * @param pos
	 * @return the stem of the word or null if it doesn't exists
	 */
	public String getStem(String word, int pos)
	{
		String x=null;
		List<String> l=wnStemmer.findStems(word);
		if(l.isEmpty()==false)
			x=l.get(pos).replace("_", " ");
		return x;
	}
	
	/**
	 * Checks if the given lemma is also a noun in WordNet
	 * @param lemma
	 * @return true if the lemma is also a noun
	 */
	private boolean isWNNoun(String lemma)
	{
		boolean test=true;
		IIndexWord idxWord = wnDictionary.getIndexWord(lemma, POS.NOUN);
		if(idxWord!=null)
		{
			//System.out.println("LEMMA: "+lemma);
			for(IWordID iw: idxWord.getWordIDs())
			{
				//System.out.println("iw: "+iw+" - POS: "+iw.getPOS().name());
				if(!iw.getPOS().name().equals("NOUN"))
				{
					test=false;
					break;
				}
			}
		}
		else
			test=false;
		return test;
	}
	
	/**
	 * Retrieves the context words and the senses of a given text.
	 * @param text
	 * @param targetWord The target word. Can ben null.
	 * @return an array of content words
	 */
	public ArrayList<Word> getContextWords(String text, String targetWord)
	{
		boolean foundTargetWord=false;
		boolean isTargetWord=false;
		boolean foundCompounding=false;
		int positionInText=0;
		int tokenIndex=0;
		int compoundIndex=0;
		int tokenCount=0;
		int slack=0;
		final int COMPOUND_SIZE=Integer.parseInt(config.getProperties(Constants.CONF_KEY_INPUT_COMPOUNDING_SIZE));
		HashSet<String> analyzedLemmas=null;
		//temp array for compounding check
		ArrayList<String> arrayCompounding=new ArrayList<String>(COMPOUND_SIZE);
		
		//retrieves the tokens
		ArrayList<String> arrayToken=getTokenizedText(text, targetWord);
		tokenCount=arrayToken.size();
		cws=new ArrayList<Word>(tokenCount);
		analyzedLemmas=new HashSet<String>(tokenCount);
		
		try
		{
			while(true)
			{
				positionInText+=slack;
				//skip _slack_ indexes
				tokenIndex+=slack;
				isTargetWord=false;
				foundCompounding=false;
				compoundIndex=tokenIndex+COMPOUND_SIZE;
				if(compoundIndex>tokenCount)
					compoundIndex=tokenCount;
				
				//stop condition
				if(tokenIndex>=tokenCount)
				{
					//System.out.println("FOUND TARGET WORD: "+foundTargetWord+" - INDEX: "+index);
					//the target word must be in the list of context words
					if(!foundTargetWord)
					{
						arrayToken.add(tokenIndex, targetWord.toLowerCase());
						foundTargetWord=true;
					}
					else
						break;
				}
				else
				{
					if(targetWord!=null)
						foundTargetWord=false;
				}
				
				
				//checks for the compounding ..
				if(COMPOUND_SIZE>1)
				{
					arrayCompounding.clear();
					for(int i=tokenIndex;i<compoundIndex;i++)
						arrayCompounding.add(arrayToken.get(i));
					
					//controls all the possible size of the compound
					for(int i=COMPOUND_SIZE;i>1;i--)
					{
						StringBuffer sbTmpCompound=new StringBuffer();
						String tmpCompound="";
						HashSet<String> tmpCompoundSet=null;
						int tmpCompoundSize=0;
						String tmpToken="";
						for(int j=0;j<i&&j<arrayCompounding.size();j++)
						{
							tmpToken=arrayCompounding.get(j);
							//skips target word and stopword
							if(tmpToken.equalsIgnoreCase(targetWord)||stopWords.isStopword(tmpToken))
								continue;
							//tmpCompound+=" "+tmpToken;
							sbTmpCompound.append(" ").append(tmpToken);
							tmpCompoundSize++;
						}
						tmpCompound=sbTmpCompound.toString();
						tmpCompound=tmpCompound.trim();
						//if we have a compound
						if(tmpCompoundSize>1)
						{
							//System.out.println("Checking compounding for: "+tmpCompound);
							tmpCompoundSet=getWikiSenses(tmpCompound);
							//if it has some senses in wikipedia
							if(tmpCompoundSet.size()>0)
							{
								//System.out.println("Found compound: "+tmpCompound);
								slack=i;
								foundCompounding=true;
								//if it's not been already analyzed
								if(!analyzedLemmas.contains(tmpCompound))
								{
									analyzedLemmas.add(tmpCompound);
									for(String title: tmpCompoundSet)
									{
										if(!title.startsWith("List of")&&!title.startsWith("list of"))
											cws.add(new Word(tmpCompound, title, POS.NOUN, tmpCompound, positionInText, isTargetWord, tmpCompoundSet));
									}
								}
								
								break;
							}
						}
					}
				}
				
				//normal one-token check
				if(!foundCompounding)
				{
					String token=arrayToken.get(tokenIndex).trim();
					//System.out.println("TOKEN: "+token);

					//skips one-char words
					if(token.length()<=1)
					{
						slack=1;
						continue;
					}
					
					//skips numbers
					try
					{
						Integer.parseInt(token);
						slack=1;
						continue;
					}
					catch(NumberFormatException e)
					{}
					
					//lemmatize
					String lemma=lemmatize(token);
					
					//skips stopwords and non-nouns
					if((!stopWords.isStopword(lemma)&&isWNNoun(lemma))||foundTargetWord)
					{
						//skips already analyzed ones
						if(analyzedLemmas.contains(lemma))
						{
							slack=1;
							continue;
						}
						
						analyzedLemmas.add(lemma);
						
						if(targetWord==null||lemma.equalsIgnoreCase(targetWord))
							foundTargetWord=true;
						
						if(foundTargetWord)
							isTargetWord=true;
					
						//System.out.println("LEMMA: "+lemma+" - ITW: "+isTargetWord);
						HashSet<String> senses=getWikiSenses(lemma);
						for(String title: senses)
						{
							if(!title.startsWith("List of")&&!title.startsWith("list of"))
								cws.add(new Word(token, title, POS.NOUN, lemma, positionInText, isTargetWord, senses));
						}
					}
					slack=1;
				}
				
			}//while
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cws;
	}
	
	/**
	 * Lemmatize a token
	 * @param token
	 * @return the lemma of a token
	 */
	private String lemmatize(String token)
	{
		String lemma=null;
		if(token.equals("mars"))
			lemma=token;
		else
		{
			lemma=getLemma(token);
			if(lemma==null)
				lemma=token;
			lemma=lemma.toLowerCase();
		}
		return lemma;
	}
	
	/**
	 * Calculates the content graph of these content words.
	 * @param contentWords
	 * @param cg the Context graph algorithm
	 * @return The content graph
	 * @throws Exception
	 */
	public Neo4jGraph calculateContentGraph(ArrayList<Word> contentWords, ContextGraphGenerator cg) throws Exception
	{
		return cg.generate(contentWords);
	}
	
	/**
	 * Applies the ranking algorithm to the context graph.
	 * @param graph The graph
	 * @param routines if null use standard routines
	 * @throws Exception
	 */
	public void rankalo(Neo4jGraph graph, TSCommonRoutines routines) throws Exception
	{
		System.out.println("\n - Ranking ... ");
		long startTime=System.currentTimeMillis();
		//the graph to rank is the same (same physical path) of the original?
		if(routines==null)
			analyzer.calculateCentrality(graph);
		else
			analyzer.calculateCentrality(graph, routines);
		
		System.out.println("Ranking time: "+(double)(System.currentTimeMillis()-startTime)/60000+" min.");
	}
	
	/**
	 * Finds the disambiguated content Words.
	 * @param tsRoutines the routines to use
	 * @return an array of words
	 */
	public ArrayList<Word> getDisambiguatedContentWords(TSCommonRoutines tsRoutines)
	{
		ArrayList<Word> dcw=new ArrayList<Word>();
		manager=CentralityManager.getInstance();
		
		//for each content word, find best ranked sense
		int lastPos=-1;
		Word bw=null;
		for(Word w: cws)
		{
			if(lastPos!=w.getPosition())
			{
				bw=getBestRakedSense(w, tsRoutines);
				if(bw!=null)
					dcw.add(bw);
				else
				{
					dcw.clear();
					break;
				}
			}
			lastPos=w.getPosition();
		}
		//resets centrality
		manager.shutdown();
		return dcw;
	}
	
	/**
	 * Finds the disambiguated content Words using internal routines.
	 * @return an array of words
	 */
	public ArrayList<Word> getDisambiguatedContentWords()
	{
		return getDisambiguatedContentWords(tsRoutines);
	}
	
	/**
	 * Retrieves the best ranked node and builds a word object with it.
	 * @param w The word to disambiguate
	 * @return the choosed word for the best-ranked node/sense.
	 */
	public Word getBestRakedSense(Word w, TSCommonRoutines tsRoutines)
	{
		boolean isFirst=true;
		boolean allZero=true;
		boolean checkNonzeroMV=Boolean.parseBoolean(config.getProperties(Constants.CONF_KEY_DISAMBIGUATION_NONZERO_MV));
		int sensesCount=0;
		Word bestRankedWord=new Word();
		double bestRankValue=0d;
		Double tmpRank=0d;
		String tmpNodeName=null;

		Transaction tx=tsRoutines.getTransaction();
		try
		{
			Node tmpNode=null;
			//HashSet<String> senses=getWikiSenses(w.getLemma());
			HashSet<String> senses=w.getSenses();
			senses=removeGarbagePages(senses);
			sensesCount=senses.size();

			//for each sense of the lemma
			for(String title: senses)
			{
				if(isFirst)
				{
					tmpNodeName=title;
					isFirst=false;
				}
				
				LinkedList<Node> l=tsRoutines.getNodes(GraphNodeProperties.ID.toString(), title.toLowerCase());
				if(l!=null&&l.isEmpty()==false)
					tmpNode=l.getFirst();
				else
					tmpNode=null;
				//if a node is not connected the centrality is 0 (NULL is returned..)
				if((tmpRank=manager.getCentrality(tmpNode))==null)
					tmpRank=0d;
				if(tmpRank<0)
					tmpRank*=-1;
				//don't consider 0-valued results
				if(tmpRank.doubleValue()!=0&&bestRankValue<=tmpRank.doubleValue())
				{
					allZero=false;
					bestRankValue=tmpRank;
					tmpNodeName=title;
				}
				System.out.println("LEMMA: "+w.getLemma()+" - NODE: "+title+" - CENTRALITY: "+tmpRank);
			}
			
			//strategia di scelta del senso quando tutti quelli noti sono scollegati
			if(allZero)
			{
				//zero valued results are considered without context-graph
				if(checkNonzeroMV&&w.isTargetWord())
					return null;
				
				//randomly choose a winner sense
				Random r=new Random(1981);
				int index=r.nextInt(sensesCount);
				int i=0;
				for(String title: senses)
				{
					if(i==index)
					{
						tmpNodeName=title;
						break;
					}
					i++;
				}
			}
			
			//set up the best-ranked sense for this word
			bestRankedWord.setPos(w.getPos());
			bestRankedWord.setSense(tmpNodeName);
			bestRankedWord.setText(w.getText());
			bestRankedWord.setLemma(w.getLemma());
			bestRankedWord.setPosition(w.getPosition());
			bestRankedWord.setRanking(bestRankValue);
			bestRankedWord.setTargetWord(w.isTargetWord());
			
			tx.success();
		}
		catch(Exception e)
		{
			tx.failure();
			e.printStackTrace();
		}
		finally
		{
			tx.finish();
		}
		
		return bestRankedWord;
	}
	
	/**
	 * Removes the not necessary pages (senses)
	 * @param senses
	 * @return the new set
	 */
	private HashSet<String> removeGarbagePages(HashSet<String> senses)
	{
		HashSet<String> set=new HashSet<String>();
		for(String s: senses)
		{
			if(!s.startsWith("List of"))
				set.add(s);
		}
		return set;
	}
	
	/**
	 * Calculates the Lesk value
	 * @param contentWords
	 * @param s
	 * @return a value in [0, 1]
	 */
	public double getLeskValue(ArrayList<Word> contentWords, ArrayList<Word> s)
	{
		double l=0d;
		int n=0;
		int count=0;
		String lastLemma="";
		
		for(Word w: contentWords)
		{
			if(!lastLemma.equals(w.getLemma()))
			{
				n++;
				lastLemma=w.getLemma();
				for(Word ww: s)
				{
					if(w.getLemma().equals(ww.getLemma()))
					{
						//System.out.println(w.getLemma());
						count++;
					}
				}
			}
		}
		l=(double)count/n;
		return l;
	}
	
	/**
	 * The Lesk senses
	 * @param targetWord
	 * @return
	 */
	public ArrayList<ArrayList<Word>> getLeskSenses(String targetWord)
	{
		ArrayList<ArrayList<Word>> s=null;
		String lemma=getLemma(targetWord);
		if(lemma==null)
			lemma=targetWord;
		lemma=lemma.toLowerCase();

		try
		{
			HashSet<String> senses=getWikiSenses(lemma);
			s=new ArrayList<ArrayList<Word>>(senses.size());
			for(String sense: senses)
			{
				ArrayList<Word> w=new ArrayList<Word>();
				HashSet<String> a=new HashSet<String>();
				
				//the first is the sense
				w.add(new Word(sense, sense, POS.NOUN, lemma, 0, true));
				
				WikiPage wp=sw.getPageByTitle(sense);
				WikiText wt = wp.getText();
				for(WikiWord ww : wt.getWikiWords())
				{
					String text=ww.getWikiText();
					String sz=ww.getWikiSense();
					String lz=sw.getLemmaFromTitle(sz);
					if(!sz.isEmpty()&&!lemma.equals(lz)&&!a.contains(lz))
					{
						w.add(new Word(text, sz, POS.NOUN, lz, 0, false));
						a.add(lz);
					}
				}
				
				s.add(w);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return s;
	}
	
	/**
	 * Writes a graph to the disk
	 * @param graph The graph
	 */
	public void writeGraph(Neo4jGraph graph)
	{
		String path=graph.getPhysicalPath();
		File f=new File(path);
		if(!f.isDirectory())
		{
			Neo4jGraphWriter writer=new Neo4jGraphWriter();
			System.out.println("Writing context graph to: "+path);
			System.out.println("Nodes: "+graph.getNodeSet().size()+" -- Edges: "+graph.getEdgeSet().size());
			
			if(writer.writeGraph(graph, path, tsRoutines))
				System.out.println("OK.");
			else
				System.out.println("KO.");
		}
		else
			System.out.println("The graph already exists!");
	}
	
	/**
	 * Checks for the existence of this graph
	 * @param graphID The graph id
	 * @return true if exists.
	 */
	public boolean existGraph(String graphID)
	{
		String path=getGraphPath(graphID);
		boolean exist=false;
		File f=new File(path);
		if(f.isDirectory())
			exist=true;
		System.out.println("Checking existence of context-graph: "+path+" ... ");
		System.out.println(exist);
		return exist;
	}
	
	/**
	 * Determines the path for the context graph
	 * @param graphID
	 * @return The physical path
	 */
	public String getGraphPath(String graphID)
	{
		String path=config.getProperties(Constants.CONF_KEY_CONTEXT_GRAPH_PATH)+"/"+graphID;
		return path;
	}
	
	/**
	 * Reads the entire graph
	 * @param graphID the graph id
	 * @return The graph read from disk
	 */
	public Neo4jGraph readGraph(String graphID, TSCommonRoutines routines)
	{
		String path=getGraphPath(graphID);
		System.out.println("Reading context graph from: "+path);
		Neo4jGraphReader reader=new Neo4jGraphReader(routines);
		Neo4jGraph g=null;

		if((g=reader.read(path))!=null)
			System.out.println("OK. Nodes: "+g.getNodeSet().size()+" - Edges: "+g.getEdgeSet().size());
		else
			System.out.println("KO.");
		return g;
	}
	
	/**
	 * Returns the internal traverser.
	 * @return The traverser object.
	 */
	public Traverser getTraverser()
	{
		return traverser;
	}
	
	/**
	 * The internal transaction safe routines.
	 * @return The internal transaction safe routines. 
	 */
	public TSCommonRoutines getTSRoutines()
	{
		return tsRoutines;
	}
	
	/**
	 * Retrieves the node object
	 * @param node
	 * @return The node object
	 */
	public Node getNode(String node)
	{
		return tsRoutines.getNode(node);
	}
}

