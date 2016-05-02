package schemamatchings.ontobuilder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.modica.ontology.Ontology;
import com.modica.ontology.algorithm.AbstractAlgorithm;
import com.modica.ontology.algorithm.Algorithm;
import com.modica.ontology.algorithm.AlgorithmException;
import com.modica.ontology.algorithm.AlgorithmUtilities;
import com.modica.ontology.match.MatchInformation;

/**
 * This class provides an interface for the user to compare between
 * a target and a candidate ontologies using a matching algorithm.
 *
 */
public final class OntoBuilderWrapper extends OntoBuilder{

  private URL candidateURL;
  private URL targetURL;
  private Ontology candidateOntology;
  private Ontology targetOntology;


  private String defaultAlgorithm = MatchingAlgorithms.TERM;
  private boolean useThesaurus = true;
  private boolean newCandidate = true;

  private Algorithm algorithm;


 /**
 * Default values: <br>
 * 	defaultAlgorithm: MatchingAlgorithms.TERM (the matching algorithm) <br>
 * 	useThesaurus: true - should Thesaurus be used <br>
 * 	newCandidate: true - a flag to prevent recreating an Ontology from the same URL
 */
public OntoBuilderWrapper(){
    super();
  }
 
/**
 * Creates a light OntoBuilderWrapper
 * Default values: <br>
 * 	defaultAlgorithm: MatchingAlgorithms.TERM (the matching algorithm) <br>
 * 	useThesaurus: true - should Thesaurus be used <br>
 * 	newCandidate: true - a flag to prevent recreating an Ontology from the same URL
 */
  public OntoBuilderWrapper(boolean light) {
      super(light);
  }

  /** 
 * @return AbstractAlgorithm - algorithm for matching the ontologies
 */
public AbstractAlgorithm getUsedAlgorithm(){
    return (AbstractAlgorithm)algorithm;
  }

  /**
 * @param algorithmToUse set the desired algorithm for matching the ontologies
 * @return AbstractAlgorithm 
 * @throws AlgorithmException
 */
public AbstractAlgorithm loadMatchAlgorithm(String algorithmToUse) throws AlgorithmException{
     return (AbstractAlgorithm)AlgorithmUtilities.getAlgorithmsInstance(new File("algorithms.xml"),algorithmToUse);
  }


  /**
 * @param candidateURL the URL to parse a candidate ontology from 
 */
public void setCandidantURL(URL candidateURL){
    if (this.candidateURL != null && this.candidateURL.equals(candidateURL))
      newCandidate = false;
    else
      newCandidate = true;
    this.candidateURL = candidateURL;
  }


  /**
 * @param targetURL the URL to parse a target ontology from
 */
public void setTargetURL(URL targetURL){
    this.targetURL = targetURL;
  }

  /**
 * @param algorithmToUse set the desired algorithm for matching the ontologies
 * @return MatchInformation - holds the results of the matches
 * @throws OntoBuilderWrapperException
 */
public MatchInformation matchOntologies(String algorithmToUse) throws OntoBuilderWrapperException{
    try{
      if (candidateURL == null || targetURL == null) return null;
      else{
        algorithm=AlgorithmUtilities.getAlgorithmsInstance(new File("algorithms.xml"),algorithmToUse);
        if(algorithm==null){
          return null;
        }
        System.out.println("matching process starts...uses algorithm:"+algorithm.getName());
        targetOntology = Ontology.generateOntology(targetURL);
        System.out.println("target ontology generation finished...");
        if(targetOntology==null){
          return null;
        }
        if (newCandidate){
             candidateOntology = Ontology.generateOntology(candidateURL);
             System.out.println("candidate ontology generation finished...");
        }
        else{
             System.out.println("candidate ontology has already been generated...");
        }
        if(candidateOntology==null){
          return null;
        }
        System.out.println("normalizing ontologies...");
        targetOntology.normalize();
        candidateOntology.normalize();
        System.out.println("match process finished...");
        return candidateOntology.match(targetOntology,algorithm);
      }
    }catch(Throwable e){
      throw new OntoBuilderWrapperException(e.getMessage());
    }

  }


/**
 * @param candidateOntology the URL to parse a candidate ontology from
 * @param targetOntology the URL to parse a target ontology from
 * @param algorithmToUse set the desired algorithm for matching the ontologies
 * @return MatchInformation - holds the results of the matches
 * @throws OntoBuilderWrapperException
 */
public MatchInformation matchOntologies(Ontology candidateOntology,Ontology targetOntology,String algorithmToUse) throws OntoBuilderWrapperException{
       try{
        algorithm=AlgorithmUtilities.getAlgorithmsInstance(new File("algorithms.xml"),algorithmToUse);
        if(algorithm==null){
          return null;
        }
        System.out.println("matching process starts...uses algorithm:"+algorithm.getName());
        System.out.println("normalizing ontologies...");
        targetOntology.normalize();
        candidateOntology.normalize();
        MatchInformation res = candidateOntology.match(targetOntology,algorithm); 
        System.out.println("match process finished...");
        return res;
       }catch(Throwable e){
         throw new OntoBuilderWrapperException(e.getMessage());
       }
  }

  /** 
 * @param candidateURL The URL which a candidate ontology will be generated from
 * @param targetURL The URL which a target ontology will be generated from
 * @param matchAlgorithm set the desired algorithm for matching the ontologies 
 * @return MatchInformation - holds the results of the matches
 * @throws OntoBuilderWrapperException
 */
public MatchInformation matchOntologies(URL candidateURL,URL targetURL,String matchAlgorithm)throws OntoBuilderWrapperException{
    setCandidantURL(candidateURL);
    setTargetURL(targetURL);
    return matchOntologies(matchAlgorithm);
  }

  /**
 * @param candidateURL The URL which a candidate ontology will be generated from
 * @param targetURL The URL which a target ontology will be generated from
 * @return MatchInformation - holds the results of the matchess
 * @throws MalformedURLException
 * @throws OntoBuilderWrapperException
 */
public MatchInformation matchOntologies(String candidateURL,String targetURL) throws MalformedURLException,OntoBuilderWrapperException{
    setCandidantURL(new URL(candidateURL));
    setTargetURL(new URL(targetURL));
    return matchOntologies(defaultAlgorithm);
  }

  /**
 * @param defaultAlgorithm the name of a matching algorithm 
 * from the list at: MatchingAlgorithms interface.
 */
  public void setDefaultAlgorithm(String defaultAlgorithm){
    this.defaultAlgorithm = defaultAlgorithm;
  }

  /**
   * returns the default matching algorithm
   * @return String with the name of the default algorithm
   */
  public String getDefaultAlgorithm(){
    return defaultAlgorithm;
  }

  /**
 * @param candidateURL The URL which a candidate ontology will be generated from
 * @param targetURL The URL which a target ontology will be generated from
 * @param algorithmName set the desired algorithm for matching the ontologies
 * @return MatchInformation holds the results of the matches
 * @throws MalformedURLException
 * @throws OntoBuilderWrapperException
 */
public MatchInformation matchOntologies(String candidateURL,String targetURL,String algorithmName)throws MalformedURLException,OntoBuilderWrapperException{
    setCandidantURL(new URL(candidateURL));
    setTargetURL(new URL(targetURL));
    return matchOntologies(algorithmName);
  }

  /**
 * @param useThesaurus should Thesaurus be used
 */
public void setUseThesaurus(boolean useThesaurus){
    this.useThesaurus = useThesaurus;
  }
  
  /**
   * Extracts and Normalized Ontology from an .xml
   * @param filepath the file path to the desired xml.file
   * @return Ontology - an ontology object which is built from the .xml file 
   * @throws IOException
   */
  public Ontology readOntologyXMLFile(String filepath) throws IOException{
    return readOntologyXMLFile(filepath,true);
  }
  
  /**
   * Extracts and ontology from an .xml
   * @param filepath the file path to the desired xml.file
   * @param normalize 
   * @return Ontology
   * @throws IOException
   */
  public Ontology readOntologyXMLFile(String filepath,boolean normalize) throws IOException{
    Ontology ontology =  Ontology.openFromXML(new File(filepath));
    if (normalize)
      ontology.normalize();
    return ontology;
  }

  /**
 * @param siteURL provide a URL to create an Ontology from
 * @param normalize Normalize ontology?
 * @return Ontology - an ontology object built from the URL provided
 * @throws OntoBuilderWrapperException
 */
public Ontology generateOntology(String siteURL,boolean normalize) throws OntoBuilderWrapperException{
    try{
      Ontology toReturn = Ontology.generateOntology(new URL(siteURL));
      if (toReturn == null) return null;
      if (normalize)
        toReturn.normalize();
      return toReturn;
    }catch(Exception e){
      throw new OntoBuilderWrapperException(e.getMessage());
    }
  }

  /**
 * @param siteURL provide a URL to create an Ontology from
 * @param normalize Normalize ontology?
 * @param outputFilepath the folder to write the ontology to (.xml file)
 * @return Ontology - an ontology object built from the URL provided 
 * @throws OntoBuilderWrapperException
 */
public Ontology generateOntology(String siteURL,boolean normalize,String outputFilepath) throws OntoBuilderWrapperException{
    try{
      Ontology toReturn = generateOntology(siteURL,normalize);
      if (toReturn == null) return null;
      toReturn.saveToXML(new File(outputFilepath));
      return toReturn;
    }catch(Exception e){
      throw new OntoBuilderWrapperException(e.getMessage());
    }
  }
}
