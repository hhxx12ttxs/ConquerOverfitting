package org.gbif.ecat.benchmark;

import org.gbif.utils.file.FileUtils;

import org.apache.commons.io.LineIterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Random;
import java.util.regex.Pattern;

public class BenchmarkLucene {
  private static final String ENV_DATA_FILE = "SOURCE_FILE";
  private static final String ENV_INDEX_DIR = "WRITE_DIR";
  private static final String ENV_NUM_QUERY = "NUM_QUERY";
  private static final String ENV_NUM_FQUERY = "NUM_FQUERY";
  private static final Pattern colSplit = Pattern.compile("[\t\01]");
  private static final String LFIELD_ID = "id";
  private static final String LFIELD_PROVIDER_ID = "providerId";
  private static final String LFIELD_RESOURCE_ID = "resourceId";
  private static final String LFIELD_SCINAME = "scientificName";
  private static final String LFIELD_KINGDOM = "kingdom";
  private static final String LFIELD_COUNTRY = "country";
  private static final String LFIELD_ALL = "fulltext";
  private static final String LFIELD_RANDOM = "random";
  private static int hitsPerPage = 10;
  private static Random rnd = new Random();
  private static String vowels = "aeiou";

  private final File source;
  private final File writeDir;
  private final Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
  private final Directory dir;

  public BenchmarkLucene(File source, File writeDir) throws IOException {
    super();
    this.source = source;
    this.writeDir = writeDir;
    dir = new NIOFSDirectory(new File(writeDir, "lucene"));
  }

  private static File getPropAsFile(String prop) {
    String src = System.getProperty(prop);
    if (src != null) {
      File f = new File(src);
      if (f.exists()) {
        return f;
      }
    }
    return null;
  }

  /**
   * @param envNumQuery
   * @param i
   * @return
   */
  private static int getPropAsInt(String prop, int i) {
    String src = System.getProperty(prop);
    if (src != null) {
      i = Integer.parseInt(src);
    }
    return i;
  }

  public static void main(String[] args) throws IOException, SQLException {
    // /user/hive/warehouse/raw_occurrence_record
    File src = getPropAsFile(ENV_DATA_FILE);
    File dir = getPropAsFile(ENV_INDEX_DIR);
    if (src == null || dir == null) {
      System.out.println("Please set valid java properties for " + ENV_DATA_FILE + " and " + ENV_INDEX_DIR);
      System.exit(0);
    }

    BenchmarkLucene b = new BenchmarkLucene(src, dir);
    System.out.println("Benchmarking LUCENE");
    System.out.println("Building lucene index from " + src.getAbsolutePath());
    b.writeIndex();

    int x = getPropAsInt(ENV_NUM_QUERY, 10000);
    System.out.println("Random " + x + " reads ...");
    b.randomIndexLookup(x, true);

    x = getPropAsInt(ENV_NUM_FQUERY, 1000);
    System.out.println("Random " + x + " fuzzy reads ...");
    b.randomFuzzyIndexLookup(x, true);
    System.out.println("Done.");
  }

  public void randomFuzzyIndexLookup(int queries, boolean retrieveDocs) throws CorruptIndexException, IOException {
    long start = System.currentTimeMillis();
    IndexSearcher searcher = new IndexSearcher(dir, true);
    Term t = new Term(LFIELD_ALL);
    long retrievedDocs = 0;
    int x = queries;
    while (x > 0) {
      x--;
      t = t.createTerm(randomFuzzyQueryString());
      Query q = new FuzzyQuery(t);
      TopDocs top = searcher.search(q, hitsPerPage);
      ScoreDoc[] docs = top.scoreDocs;
      // retrieve docs ?
      if (retrieveDocs) {
        for (ScoreDoc d : docs) {
          Document doc = searcher.doc(d.doc);
          retrievedDocs++;
        }
      }
    }
    long end = System.currentTimeMillis();
    System.out.println(String.format(" Searched %s fuzzy queries in %s ms, retrieving %s documents", queries, (end - start), retrievedDocs));

  }

  private String randomFuzzyQueryString() {
    int len = 5 + rnd.nextInt(4);
    char[] x = new char[len];
    while (len > 0) {
      len--;
      int a = rnd.nextInt(24) + 97;
      x[len] = (char) a;
    }
    return String.valueOf(x);
  }

  public void randomIndexLookup(int queries, boolean retrieveDocs) throws IOException {

    long start = System.currentTimeMillis();
    IndexSearcher searcher = new IndexSearcher(dir, true);
    Term t = new Term(LFIELD_ALL);
    long retrievedDocs = 0;
    int x = queries;
    while (x > 0) {
      x--;
      t = t.createTerm(randomQueryString() + "*");
      Query q = new WildcardQuery(t);
      TopDocs top = searcher.search(q, hitsPerPage);
      ScoreDoc[] docs = top.scoreDocs;
      // retrieve docs ?
      if (retrieveDocs) {
        for (ScoreDoc d : docs) {
          Document doc = searcher.doc(d.doc);
          retrievedDocs++;
        }
      }
    }
    long end = System.currentTimeMillis();
    System.out.println(String.format(" Searched %s 3-char wildcard queries in %s ms, retrieving %s documents", queries, (end - start), retrievedDocs));
  }

  private String randomQueryString() {
    char[] x = new char[3];
    int a = rnd.nextInt(24) + 97;
    int b = rnd.nextInt(5);
    int c = rnd.nextInt(24) + 97;

    x[0] = (char) a;
    x[1] = vowels.charAt(b);
    x[2] = (char) c;

    return String.valueOf(x);
  }

  public void writeIndex() throws CorruptIndexException, LockObtainFailedException, IOException {
    IndexWriter writer = new IndexWriter(dir, analyzer, true, MaxFieldLength.UNLIMITED);
    LineIterator it = new LineIterator(FileUtils.getUtf8Reader(source));

    long count = 0;
    long noColumns = 0;
    long start = System.currentTimeMillis();
    while (it.hasNext()) {
      String line = it.nextLine();
      String[] cols = colSplit.split(line);
      Document doc = new Document();
      if (cols.length >= 28) {
        doc.add(new Field(LFIELD_ID, cols[0], Store.YES, Index.NOT_ANALYZED));
        doc.add(new Field(LFIELD_PROVIDER_ID, cols[1], Store.YES, Index.NOT_ANALYZED));
        doc.add(new Field(LFIELD_RESOURCE_ID, cols[2], Store.YES, Index.NOT_ANALYZED));
        doc.add(new Field(LFIELD_SCINAME, cols[7], Store.YES, Index.ANALYZED));
        doc.add(new Field(LFIELD_KINGDOM, cols[10], Store.YES, Index.ANALYZED));
        doc.add(new Field(LFIELD_COUNTRY, cols[28], Store.YES, Index.ANALYZED));
      } else {
        noColumns++;
      }
      doc.add(new Field(LFIELD_RANDOM, randomFuzzyQueryString(), Store.YES, Index.NOT_ANALYZED));
      doc.add(new Field(LFIELD_ALL, line, Store.YES, Index.ANALYZED));
      writer.addDocument(doc);
      count++;

    }
    writer.commit();
    long endAdding = System.currentTimeMillis();
    writer.optimize();
    writer.close();
    long end = System.currentTimeMillis();

    System.out.println(String.format(" Adding %s records (%s couldnt be split) in %s ms. Optmising index in %s ms", count, noColumns, (endAdding - start),
        (end - endAdding)));
  }
}

