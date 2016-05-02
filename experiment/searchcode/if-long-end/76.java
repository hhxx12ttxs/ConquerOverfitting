/*
 * Copyright 2011 Global Biodiversity Information Facility (GBIF)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gbif.ecat.fuzzy;

import org.gbif.ecat.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import org.apache.commons.io.LineIterator;


public class FuzzyNameMatcherBenchmark {

  private static final Pattern TAB_DELIMITED = Pattern.compile("\t");
  private final String[] realTestNames =
    {"Abies alba", "Chorites", "Acantophora", "Zenionidae", "Chordata", "Danaus plexippus", "Festuca vulgaris",
      "Puma concolor", "Oenanthe linearis sinensis", "Oenanthe thomsonii"};


  private void benchmarkLuceneStandard(List<FuzzyNameMatch> names, int topDocs, int... prefixes) {
    long start = System.currentTimeMillis();
    System.out.println("\n\nInitializing new lucene fuzzy matcher with standard analyzer ...");
    FuzzyNameMatcherLucene ml = new FuzzyNameMatcherLucene(names);
    long end = System.currentTimeMillis();
    System.out.println(" Initialized in " + (end - start) + "ms\n");

    for (int p : prefixes) {
      System.out.println(String.format("Benchmarking with prefix=%d, topDocs=%d", p, topDocs));
      ml.setPrefixLength(p);
      ml.setTopDocs(topDocs);
      benchmarkOne(ml);
    }
  }

  private void benchmarkLuceneKeyword(List<FuzzyNameMatch> names, int topDocs, int... prefixes) {
    long start = System.currentTimeMillis();
    System.out.println("\n\nInitializing new lucene fuzzy matcher with keyword analyzer ...");
    FuzzyNameMatcherLuceneCompleteName mlc = new FuzzyNameMatcherLuceneCompleteName(names);
    long end = System.currentTimeMillis();
    System.out.println(" Initialized in " + (end - start) + "ms\n");

    for (int p : prefixes) {
      System.out.println(String.format("Benchmarking with prefix=%d, topDocs=%d", p, topDocs));
      mlc.setPrefixLength(p);
      mlc.setTopDocs(topDocs);
      benchmarkOne(mlc);
    }

  }


  private void benchmark(File nubNames) throws IOException {
    final int repeat = 1;
    List<FuzzyNameMatch> names = readNubNames(nubNames);

    // lucene standard analyzer
    for (int x = 0; x < repeat; x++) {
      benchmarkLuceneStandard(names, 100, 3, 2, 1);
    }

    // lucene standard analyzer
    for (int x = 0; x < repeat; x++) {
      benchmarkLuceneStandard(names, 25, 3, 2, 1);
    }

    // lucene keyword analyzer
    for (int x = 0; x < repeat; x++) {
      benchmarkLuceneKeyword(names, 100, 3, 2, 1);
    }

    // lucene keyword analyzer
    for (int x = 0; x < repeat; x++) {
      benchmarkLuceneKeyword(names, 25, 3, 2, 1);
    }

    System.out.println("\n\n\nDONE");
  }

  private void benchmarkOne(FuzzyNameMatcher m) {
    System.out.println(" querying with 10 real names");
    benchmarkOneWithTestName(m, realTestNames, 100);

    System.out.println(" querying with 1000 random names");
    benchmarkOneWithTestName(m, randomTestNames(1000), 1);
  }

  private void benchmarkOneWithTestName(FuzzyNameMatcher m, String[] testNames, int loop) {
    //System.out.println("Querying for straight matches ...");
    long start = System.currentTimeMillis();
    int runs = loop * testNames.length;
    for (int i = 0; i < loop; i++) {
      for (String n : testNames) {
        List<FuzzyNameMatch> matches = m.straightMatch(n);
        m.straightMatch(n);
      }
    }
    long end = System.currentTimeMillis();
    System.out.println(String
      .format("  %dx%d straight matches took %dms, average=%d", loop, testNames.length, (end - start),
        (end - start) / runs));

    //System.out.println("Querying for fuzzy 0.9 matches ...");
    start = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      for (String n : testNames) {
        List<FuzzyNameMatch> matches = m.fuzzyMatch(n, 0.9f);
        m.straightMatch(n);
      }
    }
    end = System.currentTimeMillis();
    System.out.println(String
      .format("  %dx%d fuzzy 0.9 matches took %dms, average=%d", loop, testNames.length, end - start,
        (end - start) / runs));

    //System.out.println("Querying for fuzzy 0.8 matches ...");
    start = System.currentTimeMillis();
    for (int i = 0; i < loop; i++) {
      for (String n : testNames) {
        List<FuzzyNameMatch> matches = m.fuzzyMatch(n, 0.8f);
        m.straightMatch(n);
      }
    }
    end = System.currentTimeMillis();
    System.out.println(String
      .format("  %dx%d fuzzy 0.8 matches took %dms, average=%d", loop, testNames.length, end - start,
        (end - start) / runs));
  }

  private List<FuzzyNameMatch> readNubNames(File nubNames) throws IOException {
    System.out.println("Reading nub names from " + nubNames.getAbsolutePath() + " ...");

    InputStream source = new FileInputStream(nubNames);
    List<FuzzyNameMatch> names = new ArrayList<FuzzyNameMatch>();

    LineIterator lines = new LineIterator(new BufferedReader(new InputStreamReader(source, "UTF-8")));
    while (lines.hasNext()) {
      String line = lines.nextLine();
      String[] parts = TAB_DELIMITED.split(line);
      names.add(new FuzzyNameMatch(Integer.parseInt(parts[0]), parts[1]));
    }

    lines.close();
    source.close();

    System.out.println("Read " + names.size() + " nub names");
    return names;
  }

  public static void main(String[] args) throws IOException {
    FuzzyNameMatcherBenchmark bench = new FuzzyNameMatcherBenchmark();
    bench.benchmark(new File(args[0]));
  }


  private String[] randomTestNames(int number) {
    String[] names = new String[number];
    Random rnd = new Random();
    while (number > 0) {
      String name = StringUtils.randomString(rnd.nextInt(10) + 2);
      if (number % 50 == 0) {
        // only genus
      } else {
        name += " " + StringUtils.randomString(rnd.nextInt(10) + 4);
        if (number % 3 == 0) {
          name += " " + StringUtils.randomString(rnd.nextInt(10) + 4);
        }
      }
      names[number - 1] = name;
      number--;
    }
    return names;
  }

}

