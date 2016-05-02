/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import opt.OptionHolder;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;

/**
 *
 * @author Chris Friedline <cfriedline@vcu.edu>
 */
public class SequencingReadCollection extends ReadCollection {

    private ArrayList<SequencingRead> processedReads;
    private ArrayList<SequencingRead> badLengthReads;
    private ArrayList<SequencingRead> badTagReads;
    private ArrayList<SequencingRead> badPrimerReads;
    private ArrayList<SequencingRead> badQualityReads;
    private ArrayList<SequencingRead> allReads;
    private int goodLengthNum;
    private int goodTagNum;
    private int goodPrimerNum;
    private int goodQualityNum;
    private int originalReadNum;
    private int badLengthNum;
    private int badTagNum;
    private int badPrimerNum;
    private int badQualityNum;
    private int region;
    private HashMap<String, SequencingRead> readHash;
    private boolean filterByQuality;
    private OptionHolder oh;
    private int numberOfBases;

    public SequencingReadCollection(File readFile, TagCollection tags, PrimerCollection primers, OptionHolder oh, CountData countData) throws FileNotFoundException {
        super();
        System.out.println("Reading file " + readFile.getName());
        region = Integer.valueOf(readFile.getName().split("\\.")[0]);
        processedReads = new ArrayList<SequencingRead>();
        badLengthReads = new ArrayList<SequencingRead>();
        badTagReads = new ArrayList<SequencingRead>();
        badPrimerReads = new ArrayList<SequencingRead>();
        badQualityReads = new ArrayList<SequencingRead>();
        readHash = new HashMap<String, SequencingRead>();
        allReads = new ArrayList<SequencingRead>();
        Scanner scan = new Scanner(readFile);
        String line = null;
        SequencingRead read = null;
        while (scan.hasNext()) {
            line = scan.nextLine();
            if (!line.isEmpty()) {
                if (line.startsWith(">")) {
                    read = new SequencingRead(line);
                    allReads.add(read);
                    readHash.put(read.getID(), read);
                } else {
                    read.addSequence(line);
                }
            }
        }

        setCountData(allReads, countData);

        numberOfBases = getNumberOfBases(allReads);

        this.oh = oh;
        originalReadNum = allReads.size();
        int minLength = 200;
        int maxLength = 540;

        minLength = oh.getMinReadLength();
        maxLength = oh.getMaxReadLength();

        ArrayList<SequencingRead> goodTaggedReads = trimReadsWithBagTags(allReads, tags, region, minLength, maxLength);
        ArrayList<SequencingRead> goodPrimerReads = trimTaggedReadswithBadPrimers(goodTaggedReads, tags, region, primers);
        if (oh.getFilterByQuality().toLowerCase().startsWith("t")) {
            filterByQuality = true;
            filterReadsByQuality(readFile, oh.getSourceExtension());
        }
    }


    public int getRegion() {
        return region;
    }

    public ArrayList<SequencingRead> getProcessedReads() {
        if (filterByQuality) {
            double minQualityAverage = 20;
            minQualityAverage = oh.getMinQualityAverage(); //overwrite with given option in option holder
            ArrayList<SequencingRead> list = new ArrayList<SequencingRead>();

            for (SequencingRead read : processedReads) {
                if (read.getQualityMean() >= minQualityAverage) {
                    goodQualityNum++;
                    list.add(read);
                } else {
                    badQualityNum++;
                    badQualityReads.add(read);
                }
            }
            return list;
        }
        return processedReads;
    }

    private ArrayList<SequencingRead> trimReadsWithBagTags(ArrayList<SequencingRead> reads, TagCollection tags, int region, int minLength, int maxLength) {
        ArrayList<SequencingRead> goodReads = new ArrayList<SequencingRead>();

        for (SequencingRead read : reads) {
            String sequence = read.getSequence();
            boolean tagFound = false;
            if (sequence.length() >= minLength && sequence.length() <= maxLength) {
                goodLengthNum++;
                for (String tag : tags.getTagsForRegion(region)) {
                    if (sequence.startsWith(tag)) {
                        goodReads.add((SequencingRead) read);
                        goodTagNum++;
                        tagFound = true;
                        break;
                    }
                }

                if (!tagFound) {
                    badTagNum++;
                    badTagReads.add((SequencingRead) read);
                }

            } else {
                badLengthNum++;
                badLengthReads.add((SequencingRead) read);
            }
        }
        return goodReads;
    }

    private ArrayList<SequencingRead> trimTaggedReadswithBadPrimers(ArrayList<SequencingRead> goodTaggedReads, TagCollection tags, int region, PrimerCollection primers) {
        ArrayList<SequencingRead> list = new ArrayList<SequencingRead>();
        for (SequencingRead read : goodTaggedReads) {
            String readTag = null;
            String sequence = read.getSequence();
            for (String tag : tags.getTagsForRegion(region)) {
                if (sequence.startsWith(tag)) {
                    readTag = tag;
                    break;
                }
            }
            
            String primer;
            boolean foundPrimer = false;
            for (int length : primers.getPrimerLengths()) {
                primer = sequence.substring(readTag.length(), readTag.length() + length);
                if (primers.containsPrimer(primer)) {
                    foundPrimer = true;
                    break;
                }
            }
            
            if (foundPrimer) {
                processedReads.add(read);
                goodPrimerNum++;
            } else {
                badPrimerNum++;
                badPrimerReads.add(read);
            }
        }
        return list;
    }

    public ArrayList<SequencingRead> getAllReads() {
        return allReads;
    }

    public int getBadLengthNum() {
        return badLengthNum;
    }

    public int getBadPrimerNum() {
        return badPrimerNum;
    }

    public int getBadQualityNum() {
        return badQualityNum;
    }

    public int getBadTagNum() {
        return badTagNum;
    }

    public int getGoodLengthNum() {
        return goodLengthNum;
    }

    public int getGoodPrimerNum() {
        return goodPrimerNum;
    }

    public int getGoodQualityNum() {
        return goodQualityNum;
    }

    public int getGoodTagNum() {
        return goodTagNum;
    }

    public int getOriginalReadNum() {
        return originalReadNum;
    }

    public ArrayList<SequencingRead> getBadPrimerReads() {
        return badPrimerReads;
    }

    public ArrayList<SequencingRead> getBadTagReads() {
        return badTagReads;
    }

    public ArrayList<SequencingRead> getBadLengthReads() {
        return badLengthReads;
    }

    private void filterReadsByQuality(File readFile, String fileExtension) throws FileNotFoundException {
        System.out.println("Getting qual data for " + readFile.getName());
        fileExtension = fileExtension.replace("\\.", "");
        String qualFileName = readFile.getName().replace(fileExtension, "qual");
        Scanner scan = new Scanner(new File(readFile.getParent() + "/" + qualFileName));
        scan.useDelimiter(">");
        while (scan.hasNext()) {
            String blob = scan.next();
            Scanner blobScanner = new Scanner(blob);
            blobScanner.useDelimiter("\\s+");
            String header = blobScanner.nextLine();
            String id = header.split("\\s+")[0];
            SequencingRead read = getReadByID(id);
            SummaryStatistics stats = new SummaryStatistics();
            int total = 0;
            int bad = 0;
            double minQualityScore = oh.getMinQualityScore();
            double maxMinQualityProportion = oh.getMaxMinQualityProportion();
            
            while (blobScanner.hasNext()) {
                double d = Double.valueOf(blobScanner.next());
                stats.addValue(d);
                total++;
                if (d < minQualityScore) {
                    bad++;
                }
            }
            double percentBad = (double) bad / total;
            if (percentBad < 0.1) {
                read.addQualityMean(stats.getMean());
            } else {
                read.addQualityMean(0);
            }
        }
    }

    public SequencingRead getReadByID(String id) {
        return readHash.get(id);
    }

    public ArrayList<SequencingRead> getBadQualityReads() {
        return badQualityReads;
    }

    private int getNumberOfBases(ArrayList<SequencingRead> allReads) {
        int total = 0;
        for (Read r : allReads) {
            total += r.getSequenceLength();
        }
        return total;
    }

    public int getNumberOfBases() {
        return numberOfBases;
    }

    private void setCountData(ArrayList<SequencingRead> allReads, CountData countData) {
        for (Read r : allReads) {
            countData.addRead(r);
        }
    }
}

