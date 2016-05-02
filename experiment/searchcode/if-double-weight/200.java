package ir.vsr;

import java.io.*;
import java.util.*;
import java.lang.*;
import java.math.*;

import ir.utilities.*;
import ir.classifiers.*;


public class PageRankInvertedIndex extends InvertedIndex{


      static double weight;
        
      public static HashMap<String, Double> pageRankToDocs = new HashMap<String, Double>();
        
      public PageRankInvertedIndex(File dirFile, short docType, boolean stem, boolean feedback, double weight) {
            super(dirFile, docType, stem, feedback);
         
      }
      
      
      /**
       *    Pulls in the "pageRanks" file add the html page file name 
            and Page Rank to HashMap
       */
      public static void readInPageRankValues(File dir, short docT, boolean s)
      {
            File file = null;
          DocumentIterator docIter = new DocumentIterator(dir, docT, s);
          while (docIter.hasMoreDocuments()) {
          FileDocument doc = docIter.nextDocument();
          // Create a document vector for this document
          if ((doc.file.getName().equals("pageRanks")))
          {
            file = doc.file;
          }
          }
          try{  
              FileInputStream fstream = new FileInputStream(file);
              // Get the object of DataInputStream
              DataInputStream in = new DataInputStream(fstream);
              BufferedReader br = new BufferedReader(new InputStreamReader(in));
              String strLine;
              //Read File Line By Line
              while ((strLine = br.readLine()) != null)   
              {
             
                    String[] members = strLine.split(" ");
                    pageRankToDocs.put(members[0], Double.parseDouble(members[1]));       
              }
              
              in.close();
           }
           catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
           }    
          
      }
      
      
      /**
       *    adds in the Page Rank for the document from the HashMap created in readInPageRankValues
       */
      protected Retrieval getRetrieval(double queryLength, DocumentReference docRef, double score) {
            // Normalize score for the lengths of the two document vectors
            score = score / (queryLength * docRef.length);
            //System.out.println("pageRank: "+ pageRankToDocs.get(docRef.file.getName()) + "  weight: "+weight);
            score += (pageRankToDocs.get(docRef.file.getName()) * weight);
            // Add a Retrieval for this document to the result array
            return new Retrieval(docRef, score);
      }
      
      /**
       *    same as InvertedIndex indexDocuments(), but resticts from indexing "pageRanks" file
       */
      protected void indexDocuments() {
        if (!tokenHash.isEmpty() || !docRefs.isEmpty()) {
          // Currently can only index one set of documents when an index is created
          throw new IllegalStateException("Cannot indexDocuments more than once in the same InvertedIndex");
        }
        // Get an iterator for the documents
        DocumentIterator docIter = new DocumentIterator(dirFile, docType, stem);
        System.out.println("Indexing documents in " + dirFile);
        // Loop, processing each of the documents

        while (docIter.hasMoreDocuments()) {
          FileDocument doc = docIter.nextDocument();
          // Create a document vector for this document
          if ((doc.file.getName().equals("pageRanks")))
          {
             continue;
          }
          else
          {
            System.out.print(doc.file.getName() + ",");
            HashMapVector vector = doc.hashMapVector();
            indexDocument(doc, vector);
          }
        }
        // Now that all documents have been processed, we can calculate the IDF weights for
        // all tokens and the resulting lengths of all weighted document vectors.
        computeIDFandDocumentLengths();
        System.out.println("\nIndexed " + docRefs.size() + " documents with " + size() + " unique terms.");
      }
        
        
        /**
       *    same as InvertedIndex main but adds support for wight value in command line
       */
        public static void main(String[] args) {
        // Parse the arguments into a directory name and optional flag

        String dirName = args[args.length - 1];
        short docType = DocumentIterator.TYPE_TEXT;
        boolean stem = false, feedback = false;
        for (int i = 0; i < args.length - 1; i++) {
          String flag = args[i];
          if (flag.equals("-html"))
            // Create HTMLFileDocuments to filter HTML tags
            docType = DocumentIterator.TYPE_HTML;
          else if (flag.equals("-stem"))
            // Stem tokens with Porter stemmer
            stem = true;
          else if (flag.equals("-feedback"))
            // Use relevance feedback
            feedback = true;
          else if (flag.equals("-weight"))
          { //grabs weight value from command line
            weight = Double.parseDouble(args[i+1]);
            i++;
          }
          else {
            throw new IllegalArgumentException("Unknown flag: "+ flag);
          }
        }
        readInPageRankValues(new File(dirName), docType, stem);
        // Create an inverted index for the files in the given directory.
        PageRankInvertedIndex index = new PageRankInvertedIndex(new File(dirName), docType, stem, feedback, weight);
       
        // index.print();
        // Interactively process queries to this index.
        index.processQueries();
      }

}

