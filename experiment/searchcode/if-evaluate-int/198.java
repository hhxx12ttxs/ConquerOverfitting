package uk.ac.strath.cis.spd.buglanguage.evaluate;

/*
 * See LICENCE_BSD for licensing information.
 *
 * Copyright Steven Davies 2012
 */

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public abstract class Evaluation {
    protected static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final ProcessBuilder gitHash = new ProcessBuilder("git", "log", "-1", "--pretty=format:%H");
    private static final ProcessBuilder gitStats = new ProcessBuilder("git", "diff", "--numstat");

    protected File methods, oldMethods, bugs, oldBugs, corpus, resultsDir;
    protected Multimap<String, Long> bugsByMethod;
    protected List<Long> bugsToEvaluate;
    protected Map<Long, File> results;
    protected Map<String, String> methodsById;

    public Evaluation(String config, String name) throws Exception {
        Properties props = new Properties();
        props.load(new FileReader(config));
        methods = new File(props.getProperty("methods"));
        oldMethods = new File(props.getProperty("old_methods"));
        bugs = new File(props.getProperty("bugs"));
        oldBugs = new File(props.getProperty("old_bugs"));
        corpus = new File(props.getProperty("corpus"));
        resultsDir = new File(props.getProperty("results"), name);
        bugsByMethod = ArrayListMultimap.create();
        results = new HashMap<Long, File>();
        bugsToEvaluate = new ArrayList<Long>();
        resultsDir.mkdir();
        FileUtils.cleanDirectory(resultsDir);
        File methodIds = new File(props.getProperty("results"), "method_ids");
        methodsById = new HashMap<String, String>();
        for(String line: FileUtils.readLines(methodIds)){
            String[] parts = line.split("\\s");
            methodsById.put(parts[0].trim(), parts[1].trim());
        }
    }

    public void processLinks() throws Exception {
        List<Long> processed = new ArrayList<Long>();
        processLinks(processed, methods);
        processLinks(processed, oldMethods);
    }

    private void processLinks(List<Long> processed, File root) throws Exception {
        for(File goldSet: root.listFiles()){
            long bug = Long.parseLong(goldSet.getName().replace("GoldSet", "").replace(".txt", ""));
            if(!processed.contains(bug)){
                processed.add(bug);
                for(String method: FileUtils.readLines(goldSet)){
                    bugsByMethod.put(method, bug);
                }
            }
        }
    }

    protected void filterBugs(File root, boolean evaluate) throws Exception {
        for(File bug: root.listFiles()){
            if(bug.getName().startsWith("LongDescription")){
                long bugId = Long.parseLong(bug.getName().replace("LongDescription", "").replace(".txt", ""));
                if(evaluate && !bugsToEvaluate.contains(bugId)){
                    bugsToEvaluate.add(bugId);
                    results.put(bugId, new File(resultsDir, String.valueOf(bugId)));
                    writeMetaData(bugId);
                }
                else if(bugsToEvaluate.contains(bugId)){
                    continue;
                }
                filterBug(bug, bugId);
            }
        }
    }

    protected abstract void writeMetaData(long bugId) throws Exception;

    protected abstract void filterBug(File bug, long bugId) throws Exception;

    public void evaluate() throws Exception {
        String[] methods = corpus.list();
        Arrays.sort(methods, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return Integer.valueOf(o1).compareTo(Integer.valueOf(o2));
            }
        });
        for(String methodId: methods){
            String method = methodsById.get(methodId);
            evaluate(methodId, method);
        }
    }

    protected abstract void evaluate(String methodId, String method) throws Exception;

    protected String getGitVersion() {
        try{
            Process process = gitHash.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return reader.readLine();
        }
        catch(Exception e){
            e.printStackTrace();
            return "Unavailable";
        }
    }

    protected String getGitChanges() {
        try{
            Process process = gitStats.start();
            process.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            int[] changes = new int[3];
            while(line != null){
                String[] parts = line.split("\\s+", 3);
                if(!parts[2].startsWith("data/")){
                    try{
                        changes[0] += Integer.parseInt(parts[0]);
                        changes[1] += Integer.parseInt(parts[1]);
                    }
                    catch(NumberFormatException e){
                        // Do nothing
                    }
                    changes[2] += 1;
                }
                line = reader.readLine();
            }

            if(changes[2] == 0){
                return "No changes";
            }

            return String.format("%s files changed, %s insertions(+), %s deletions(-)", changes[2], changes[0], changes[1]);
        }
        catch(Exception e){
            e.printStackTrace();
            return "Unavailable";
        }
    }

    protected String getDbVersion() {
        return "NA";
    }

    public void cleanup() {}

    protected void print(long bugId, Object... objects) throws Exception {
        PrintWriter writer = new PrintWriter(new BufferedOutputStream(FileUtils.openOutputStream(results.get(bugId), true)), true);
        writer.println(StringUtils.join(objects, "\t"));
        writer.close();
    }
}

