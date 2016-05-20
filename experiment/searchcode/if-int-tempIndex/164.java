package cs276.pa1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;

public class BuildIndex {

  private int                        totalFileCount = 0;
  private static File                ROOT_DIR;
  private static File                OUTPUT_DIR;
  private final Map<String, Integer> docIdDict      = new LinkedHashMap<String, Integer>();
  private final Map<String, Integer> wordDict       = new LinkedHashMap<String, Integer>();

  List<String>                       blockQ         = new ArrayList<String>();

  int                                docId          = -1;
  int                                wordId         = 0;

  public static void main(final String[] args) throws IOException {
    final BuildIndex bi = new BuildIndex();
    ROOT_DIR = new File(args[0]);
    OUTPUT_DIR = new File(args[1]);

    bi.buildIndex();
  }

  @SuppressWarnings("boxing")
  private void buildIndex() throws IOException {
    createDir(OUTPUT_DIR);

    final long startTime = System.currentTimeMillis();

    for (final File dir : ROOT_DIR.listFiles()) { //for1
      //System.out.println("\nProcessing directory : " + dir);

      blockQ.add(dir.getName());

      final File blockPl = new File(OUTPUT_DIR.getAbsoluteFile(), dir.getName());
      createFile(blockPl);

      final Map<Integer, Set<Integer>> termDocumentDict = new TreeMap<Integer, Set<Integer>>();
      final long blockStartTime = System.currentTimeMillis();

      for (final File file : dir.listFiles()) { //for2
        countFile();
        final String fileName = dir.getName() + "/" + file.getName();
        docIdDict.put(fileName, ++docId);

        for (final String line : Files.readLines(file, Charsets.US_ASCII)) {
          final Iterable<String> tokens = Splitter.on(" ").split(line);
          for (final String token : tokens) { //for4
            //System.out.println("\n\nthe token is " + token);
            if (wordDict.get(token) == null) {
              wordDict.put(token, wordId);
              wordId = wordId + 1;
            }//end if

            Set<Integer> docIds = termDocumentDict.get(wordDict.get(token));

            if (docIds != null) {
              docIds.add(docIdDict.get(fileName));
            }
            else {
             
              docIds = new TreeSet<Integer>();
              docIds.add(docIdDict.get(fileName));
            }
            termDocumentDict.put(wordDict.get(token), docIds);
          }//end for4
        }//end for3
      }//end for2

      final long blockEndTime = System.currentTimeMillis();
      //System.out.println("Time to process block " + (blockEndTime - blockStartTime) + " ms");
      writeTermDocumentDictToFile(termDocumentDict, blockPl);
    }//end for1

    final long mergeStartTime = System.currentTimeMillis();

    while (true) {
      if (blockQ.size() <= 1) {
        break;
      }
      final String b1 = blockQ.remove(0);
      final String b2 = blockQ.remove(0);
      final File b1f = new File(OUTPUT_DIR, b1);
      final File b2f = new File(OUTPUT_DIR, b2);

      final String comb = b1 + b2;
      final File combf = new File(OUTPUT_DIR, comb);
      createFile(combf);

      //Read b1f and b2f into hashmaps and merge the 2 hashmaps and write to file combf
      final Map<Integer, Set<Integer>> b1fMap = getTermDictFromMap(b1f);
      final Map<Integer, Set<Integer>> b2fMap = getTermDictFromMap(b2f);

      final Map<Integer, Set<Integer>> combfMap = mergeTermDocumentDict(b1fMap, b2fMap);
    
      writeTermDocumentDictToFile(combfMap, combf);

      deleteFile(b1f);
      deleteFile(b2f);
      
      blockQ.add(comb);
    }
    final long mergeEndTime = System.currentTimeMillis();
    //System.out.println("\n\nTime for merge took  " + (mergeEndTime - mergeStartTime) + " ms");

    final String comb = blockQ.remove(0);

    //create posting.dict
    final List<String> postingLines = createPostingDict(new File(OUTPUT_DIR, comb));
    // write posting.dict
    writePostingsToFile(postingLines, new File(OUTPUT_DIR, "postings.dict"));

    //rename file to corpus.index
    final File corpusIndex = new File(OUTPUT_DIR, "corpus.index");

    final File tempIndex = new File(OUTPUT_DIR, comb);

    tempIndex.renameTo(corpusIndex);

    System.out.println(totalFileCount);

    writeDictToFile(docIdDict, new File(OUTPUT_DIR, "doc.dict"));
    writeDictToFile(wordDict, new File(OUTPUT_DIR, "word.dict"));

    final long endTime = System.currentTimeMillis();
    //System.out.println("\n\nConstruction of index took  " + (endTime - startTime) + " ms");

   
  }

  @SuppressWarnings("boxing")
  private Map<Integer, Set<Integer>> mergeTermDocumentDict(final Map<Integer, Set<Integer>> map1,
      final Map<Integer, Set<Integer>> map2) {
    Map<Integer, Set<Integer>> corpusDict = new TreeMap<Integer, Set<Integer>>();
    Set<Integer> p1 = new TreeSet<Integer>();
    Set<Integer> p2 = new TreeSet<Integer>();

    p1 = map1.keySet();
    p2 = map2.keySet();

    Object[] p1Arr = p1.toArray();
    Object[] p2Arr = p2.toArray();

    int i = 0;
    int j = 0;
    for (; i < p1Arr.length && j < p2Arr.length;) {
      Integer termId1 = (Integer) p1Arr[i];
      Integer termId2 = (Integer) p2Arr[j];
      
      if (termId1.intValue() == termId2.intValue()) {
        Set<Integer> postingList1 = map1.get(termId1);
        Set<Integer> postingList2 = map2.get(termId2);
        final Set<Integer> mergedPostings = mergePosting(postingList1, postingList2);
        corpusDict.put(termId1, mergedPostings);
        i++;
        j++;
      }
      else if (termId1.intValue() < termId2.intValue()) {
        Set<Integer> postingList1 = map1.get(termId1);
        corpusDict.put(termId1, postingList1);
        i++;
      }
      else {
        Set<Integer> postingList2 = map2.get(termId2);
        corpusDict.put(termId2, postingList2);
        j++;
      }
    }

    while (i < p1Arr.length) {
      Integer termId = (Integer) p1Arr[i];
      Set<Integer> postingList1 = map1.get(termId);
      corpusDict.put(termId, postingList1);
      i++;
    }

    while (j < p2Arr.length) {
      Integer termId = (Integer) p2Arr[j];
      Set<Integer> postingList2 = map2.get(termId);
      corpusDict.put(termId, postingList2);
      j++;
    }
    return corpusDict;
  }

  private Set<Integer> mergePosting(final Set<Integer> postingLine1, final Set<Integer> postingLine2) {
    if (postingLine2 != null) {
      postingLine1.addAll(postingLine2);

    }
    return postingLine1;
  }

  @SuppressWarnings("boxing")
  private List<String> createPostingDict(final File file) throws IOException {
    final BufferedReader br = new BufferedReader(new FileReader(file));
    String line;
    final List<String> postingLines = new ArrayList<String>();
    int i = 0;
    while ((line = br.readLine()) != null) {
      final String[] parts = Iterables.toArray(Splitter.on("\t").trimResults().split(line), String.class);
      final Integer termId = Integer.parseInt(parts[0]);

      final List<Integer> docIds = new ArrayList<Integer>();

      final Iterable<String> iterable = Splitter.on(" ").trimResults().split(parts[1]);

      for (final Iterator<String> it = iterable.iterator(); it.hasNext();) {
        final String value = it.next();
        docIds.add(Integer.parseInt(value));
      }

      final String postingLine = termId + "\t" + i + "\t" + docIds.size();

      postingLines.add(postingLine);
      i = i + line.length() + 1;
     
    }
    
    
    return postingLines;
  }

  @SuppressWarnings("boxing")
  private Map<Integer, Set<Integer>> getTermDictFromMap(final File file) throws IOException {
    final Map<Integer, Set<Integer>> termDocumentDict = new TreeMap<Integer, Set<Integer>>();
    final BufferedReader br = new BufferedReader(new FileReader(file));
    String line;

    while ((line = br.readLine()) != null) {
      final String[] parts = Iterables.toArray(Splitter.on("\t").trimResults().split(line), String.class);
      final Integer termId = Integer.parseInt(parts[0]);

      final Set<Integer> docIds = new TreeSet<Integer>();

      final String[] docIdList = Iterables.toArray(Splitter.on(" ").trimResults().split(parts[1]), String.class);
      for (final String docId : docIdList) {
        docIds.add(Integer.parseInt(docId));
      }

      termDocumentDict.put(termId, docIds);

    }
    return termDocumentDict;
  }
 
  private void writeDictToFile(final Map<String, Integer> dict, final File file) throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(file));

    for (final Map.Entry<String, Integer> entry : dict.entrySet()) {
      bw.write(entry.getKey() + "\t" + entry.getValue());
      bw.write("\n");
    }
    bw.flush();
  }

  private void writePostingsToFile(final List<String> postingLines, final File file) throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(file));
    for (final String postingLine : postingLines) {
      bw.write(postingLine + "\n");
    }
    bw.flush();
  }

  private void writeTermDocumentDictToFile(final Map<Integer, Set<Integer>> termDocDict, final File file)
      throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(file));

    for (final Map.Entry<Integer, Set<Integer>> entry : termDocDict.entrySet()) {
      StringBuffer sb = new StringBuffer();
      sb.append(entry.getKey() + "\t");
      final Set<Integer> docIdList = entry.getValue();
      int i = 0;
      Iterator<Integer> it = docIdList.iterator();
      while (it.hasNext()) {
        if (i == docIdList.size() - 1) {
          sb.append(it.next().toString());
        }
        else {
          sb.append(it.next().toString() + " ");
        }
        i++;
      }
      sb.append("\n");
      bw.write(sb.toString());

    }
    bw.close();
  }

  private void countFile() {
	    totalFileCount++;
	  }

	  private void createDir(final File dir) {
	    if (!dir.exists()) {
	      dir.mkdir();
	    }
	  }

	  private void createFile(final File file) throws IOException {
	    if (!file.exists()) {
	      file.createNewFile();
	    }
	  }

	  private void deleteFile(final File file) {
	    if (file.exists()) {
	      file.delete();
	    }
  }
	  
  private void printMap(final Map<String, Integer> dict) {
    for (final Map.Entry<String, Integer> entry : dict.entrySet()) {
      System.out.println(entry.getKey() + " : " + entry.getValue());
    }
  }

  private void printTermDocDict(final Map<Integer, Set<Integer>> termDocDict) {
    for (final Map.Entry<Integer, Set<Integer>> entry : termDocDict.entrySet()) {
      System.out.print(entry.getKey() + ":");
      final Set<Integer> docIdList = entry.getValue();

      int i = 0;
      Iterator<Integer> it = docIdList.iterator();
      while (it.hasNext()) {
        if (i != (docIdList.size() - 1)) {
          System.out.print(it.next() + " ");
        }
        else
          System.out.print(it.next());
      }

      System.out.println("");
    }
  }

  
}

