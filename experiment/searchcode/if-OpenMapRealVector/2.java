/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ProximityHouse;
import Jama.Matrix;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import GUI.clusterPanel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math.linear.OpenMapRealVector;
import org.apache.commons.math.linear.RealVectorFormat;
import org.apache.commons.math.linear.SparseRealVector;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.FSDirectory;
import org.junit.Assert;
/**
 *
 * @author Rabindra Kharel
 */
public class ProximityMatrix {   
    
    ArrayList<ArrayList<Double>> listOlists = new ArrayList<ArrayList<Double>>();
    private ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();

    static int debugCount=0;
    private int IndexCount;
    IndexReader reader;
    TermEnum termEnum;
    Term term;
    Map<String, Integer> terms;
    private int[] docIds;
    String[] collection;
    double[][] cosine;
    int filecounter = 0;
    String FolderName[]=new String[100];
    int countLust = 0;
    int smallCountOfClusterFile;
    private String destinationDir;
    
     
    public ProximityMatrix(String destinationDir,String indexDir) throws IOException {
        this.destinationDir=destinationDir;               
        reader = IndexReader.open(FSDirectory.open(new File(indexDir+"\\LuceneIndex")));
        IndexCount=reader.maxDoc();
        //IndexCount=100;
        docIds = new int[IndexCount];
        cosine = new double[ IndexCount][ IndexCount];
        collection = new String[IndexCount];
        for(int first = 0; first < IndexCount;first++)
               {   docIds[first]= first;
               
               }                                   
       // System.out.println("reader open");
        terms = new HashMap<String,Integer>();
        termEnum = reader.terms(new Term("content"));
        int pos = 0;
        while (termEnum.next()) {
        term = termEnum.term();
        if (! "content".equals(term.field()))     
          break;
        terms.put(term.text(), pos++);      
         }
    }
    
 
    
    
  private double getCosineSimilarity(DocVector d1, DocVector d2) {  
      double cosinesimilarity;
         try{
       cosinesimilarity = (d1.vector.dotProduct(d2.vector)) /
          (d1.vector.getNorm() * d2.vector.getNorm());
         }    catch(Exception e)
          
      {
          return 0.0;
      }
  return cosinesimilarity;
  }  
  
  
       public int Calculator() throws CorruptIndexException, IOException, Exception
       {
          
            int numDocs = reader.maxDoc();
            TermFreqVector[] tfvs;
            DocVector[] docs = new DocVector[IndexCount];              
            int i = 0;                
            for (int docId : docIds) {            
            try {
                tfvs = reader.getTermFreqVectors(docId);    
                Assert.assertTrue(tfvs.length == 1);
                           
            }
            catch(Exception e)
               {
                   continue;
               }        
            docs[i] = new DocVector(terms);
            for (TermFreqVector tfv : tfvs) {
            String[] termTexts = tfv.getTerms();
            int[] termFreqs = tfv.getTermFrequencies();           
            Assert.assertEquals(termTexts.length, termFreqs.length);
            for (int j = 0; j < termTexts.length; j++) {
            docs[i].setEntry(termTexts[j], termFreqs[j]);        
        }
      }
          docs[i].normalize();
          i++;     
    }
            
        for(int doc_position = 0;doc_position<IndexCount;doc_position++){ 
        for(int indexo = 0;indexo<IndexCount;indexo++)            
        {
        cosine[doc_position][indexo]=0;
        }
        
            }
            
            
            
    for(int doc_position = 0;doc_position<IndexCount;doc_position++)
    { 
        for(int indexo = doc_position;indexo<IndexCount;indexo++)
    {
         double cosineSimilarity = getCosineSimilarity(docs[doc_position], docs[indexo]);
          Document doc1 = reader.document(docIds[doc_position]);       
          String filename = doc1.get("fullpath");    
          collection[doc_position] = filename;         
          cosine[doc_position][indexo] = cosineSimilarity; 
            }
        
          clusterPanel.workProgress((int)(doc_position+1)*100/IndexCount);
    }
    
//    System.out.println("worked till cosine values");
    Matrix proximity=new Matrix(cosine);
    proximity=proximity.plus(proximity.transpose());
    clusterMaker cMaker=new clusterMaker(proximity); 
    result=cMaker.getResult();
    System.out.println("\nNo of Clusters : "+result.size());  
    System.out.println(result.toString()); 
    printfile(collection);//unpack if you want to see the real clusters 
    return numDocs;       
       }
       
       
    
       public void  printfile(String[] collection) throws IOException
       {        
                   
             
                for (int trans=0; trans < result.size(); trans++) {
                    ArrayList<Integer> lst=result.get(trans);
                    for(int k : lst )
                    {
                         File destination = new File(destinationDir+"\\clusteredDiretory\\cluster"+trans);
                   
                    try{
                    FileUtils.copyFileToDirectory(new File(collection[k]), destination);
                   
                 
                    }
                    catch(Exception e)
                    {
                    continue;
                    }
                }
         }
       reader.close();       
       
                if("yes".equals(clusterPanel.Status))
                {
                  FileUtils.forceDelete(clusterPanel.SourceDir);
                }
          }
        
       
        
       
       
       public void separator(double start,double end,String filepath,double cos) throws IOException
       {
          if(filepath!= null){
           File file = new File(filepath);
           File destination = new File("E:\\SeparatedFile"+start+ "_"+end);
           
           if(cos <start && cos >end)
           {
                   FileUtils.forceMkdir(destination);
                   FileUtils.copyFileToDirectory(file, destination);
                   System.out.append("copied");
           }
          }
       }
       
       
private class DocVector {
    public Map<String,Integer> terms;
    public SparseRealVector vector;   
    public DocVector(Map<String,Integer> terms) 
    {        
      this.terms = terms;     
      this.vector = new OpenMapRealVector(terms.size());  
     
     
    }
    public void DocVectorNull(Map<String,Integer> terms,int value)
    {
        this.terms = null;
        this.vector = new OpenMapRealVector(value);       
    
    }
    
    public void setEntry(String term, int freq) 
    {
      if (terms.containsKey(term)) {
        int pos = terms.get(term);
        vector.setEntry(pos, (double) freq);
      }
     
    }
    
    public void normalize() 
    {
      double sum = vector.getL1Norm();
      vector = (SparseRealVector) vector.mapDivide(sum);
    }
    
        @Override
    public String toString()
        {
      RealVectorFormat formatter = new RealVectorFormat();
      return formatter.format(vector);
    }
  }   


}
