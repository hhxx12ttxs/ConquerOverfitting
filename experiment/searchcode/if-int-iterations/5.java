package ktsp;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author michal
 *
 * This is the main class, which contains the main method and the auxiliary method printHelp.
 * The main function deals with command-line parameters and the creation of the basic classes like the IO classes and the Ktsp class
 * and is also responsible of calculating the ranking. It handles most of the parameter-related errors,
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Initialisation of most variables with their defauolt values, which will be changed if the corresponding option is present.
        
        // Class Random used for every random step needed. This initialisation makes the internal implementation choose a seed that is unlikely to be repeated between runs.
        Random random = new Random();

        // Booleans used to determine whether the filenames have been found in the comnand-line parameters.
        boolean outputFileFound = false;
        boolean inputFileFound = false;

        // Variables with the filenames or filepaths of the different input and output files. Initialised with an empty string which will be changed later.
        String infile = "";
        String outfile = "";
        String modfile = "";

        // Boolean that states whether the data in the main input file should be treated as isoforms or as genes
        boolean isoforms = false;

        // Number of iterations for crossvalidation.
        int iterations = -1;

        // Number of pairs to be shown in the output file with their single_pair_performance.
        int show = -1;

        // The upper limit of the k value in the ktsp algorithm.
        int kmax = -1;

        // The suffixes used to differentiate between the two classes in the sample names.
        String class1 = "N";
        String class2 = "T";

        // Booleans stating the different modes in which the program can be executed.
        boolean predictionOnly = false;
        boolean randomModel = false;
        boolean randomiseLabels = false;

        // for prediction only modes, boolean that indicates printing specific details of the predictions of each sample.
        boolean showPredictionDetails = false;

        // Variable used for the randomise labels mode to indicate the number of iterations in which the random labels mode should be executed,
        int nrandomiseLabels = -1;

        // Variable used for the random model mode to indicate the number of pairs that the randomly generated model should have.
        int nrandom = -1;

        // Processing of the command-line arguments with error messages when an inconsistency is detected.
        if (args.length == 0) {
            System.err.println("Error: At least one argument is mandatory. -h for details on possible arguments");
            System.exit(1);
        }
        else if (args.length == 1 && args[0].equals("-h")) {
            printHelp();
            System.exit(0);
        }
        else {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-i")) {
                    isoforms = true;
                }
                else if (args[i].equals("-p")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option -p should be followed by one argument");
                        System.exit(1);
                    }
                    modfile = args[++i];
                    predictionOnly = true;
                }
                else if (args[i].equals("-d")) {
                    showPredictionDetails = true;
                }
                else if (args[i].equals("-r")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option -r should be followed by one argument");
                        System.exit(1);
                    }
                    try {
                        nrandom = Integer.parseInt(args[++i]);
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Argument following -r must be an integer");
                        System.exit(1);
                    }
                    if (nrandom < 1) {
                        System.err.println("Argument following -r must be at least 1");
                        System.exit(1);
                    }
                    if (nrandom%2 != 1) {
                        System.err.println("Argument following -r must be odd");
                        System.exit(1);
                    }
                    randomModel = true;
                }
                else if (args[i].equals("-o")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option -o should be followed by one argument");
                        System.exit(1);
                    }
                    outfile = args[++i];
                    outputFileFound = true;
                }
                else if (args[i].equals("-s")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option -s should be followed by one argument");
                        System.exit(1);
                    }
                    try {
                        show = Integer.parseInt(args[++i]);

                    }
                    catch (NumberFormatException e) {
                        System.err.println("Argument following -s must be an integer");
                        System.exit(1);
                    }
                    if (show < 0) {
                        System.err.println("Argument following -s must be 0 or greeter");
                        System.exit(1);
                    }
                }
                else if (args[i].equals("-n")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option -n should be followed by one argument");
                        System.exit(1);
                    }
                    try {
                        iterations = Integer.parseInt(args[++i]);
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Argument following -n must be an integer");
                        System.exit(1);
                    }
                    if (iterations < 1) {
                        System.err.println("Argument following -n must be at least 1");
                        System.exit(1);
                    }

                }
                else if (args[i].equals("-l")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option -l should be followed by one argument");
                        System.exit(1);
                    }
                    try {
                        nrandomiseLabels = Integer.parseInt(args[++i]);
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Argument following -l must be an integer");
                        System.exit(1);
                    }
                    if (nrandomiseLabels < 0) {
                        System.err.println("Argument following -l must be at least 1");
                        System.exit(1);
                    }
                    randomiseLabels = true;
                }
                else if (args[i].equals("-k")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option -k should be followed by one argument");
                        System.exit(1);
                    }
                    try {
                        kmax = Integer.parseInt(args[++i]);
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Argument following -k must be an integer");
                        System.exit(1);
                    }
                    if (kmax < 1) {
                        System.err.println("Argument following -k must be at least 1");
                        System.exit(1);
                    }
                    else if (kmax > 200) {
                        System.err.println("Warning: It may not be advisable to use a big maximum k. The maximum k you specified with the -k option may not  be adequate for this algorithm.");
                    }
                }
                else if (args[i].equals("-c")) {
                    if (i + 2 > args.length - 1) {
                        System.err.println("Option -c should be followed by two arguments");
                        System.exit(1);
                    }
                    class1 = args[++i];
                    class2 = args[++i];
                }
                else if (args[i].equals("--seed")) {
                    if (i + 1 > args.length - 1) {
                        System.err.println("Option --seed should be followed by one argument");
                        System.exit(1);
                    }
                    try {
                        random.setSeed(Long.parseLong(args[++i]));
                    }
                    catch (NumberFormatException e) {
                        System.err.println("Argument following --seed must be an integer");
                        System.exit(1);
                    }
                }
                else if (!inputFileFound) {
                    infile = args[i];
                    inputFileFound = true;
                }
                else {
                    System.err.println("At least one of the arguments is incorrect. Try -h for details on possible arguments.");
                    System.exit(1);
                }

            }
        }

        // If there wasn't an argument corresponding to the main input file, an error is displayed.
        if (!inputFileFound) {
            System.err.println("Missing mandatory argument input filename");
            System.exit(1);

        }

        //if the option to define the output filename, a filename is constructed with the input file name and adding a different suffix depending on the mode that is being executed.
        if (!outputFileFound) {
            int dotPosition = infile.lastIndexOf('.');
            String extension = "";
            String fileName = infile;
            if (dotPosition > 0) {
                extension = infile.substring(dotPosition);
                fileName = infile.substring(0, dotPosition);
            }
            if (predictionOnly) {
                outfile = fileName + "_output_mod_" + modfile + extension;
            }
            else if (randomModel) {
                outfile = fileName + "_output_rand" + extension;
            }
            else if (randomiseLabels) {
                outfile = fileName + "_output_randlabels" + extension;
            }
            else {
                outfile = fileName + "_output" + extension;
            }
            
        }// Creating and initialising the io classes that read and write the input and output files.
        FileReader reader = new FileReader(infile);
        FileWriter writer = new FileWriter(outfile, class1, class2, reader.getSampleNames());

        // Getting the list of ids and mapping them for the two classes. Note that the variable names for the two classes use the words normal and tumor even if the two classes have different semanthics and the class suffixes have been changed..
        List<String> sampleIds = reader.getSampleNames();
        List<Integer> normalIds = new ArrayList();
        List<Integer> tumorIds = new ArrayList();
        for (int i = 0; i < sampleIds.size(); i++) {
            if (sampleIds.get(i).endsWith(class1 + "\"") || sampleIds.get(i).endsWith(class1)) normalIds.add(i);
            else if (sampleIds.get(i).endsWith(class2 + "\"") || sampleIds.get(i).endsWith(class2)) tumorIds.add(i);
            else {
                System.err.println("There's a sample id that has incorrect or no class identifier");
                System.exit(1);
            }
        }

        // For the crossvalidation to work properly, there should be at least 1 sample of each class for each iteration
        if (iterations > normalIds.size() || iterations > tumorIds.size() && !predictionOnly && !randomModel) {
            System.err.println("Option -n should specify a number of iterations no greater than the number of samples in the least-represented class");
            System.exit(1);
        }

        // Reading the input data line by line with the class FileReader.
        List<List<Double> > data = new ArrayList();
        List<Double> currentLine = new ArrayList(reader.getNextLine());
        int countLines = 0;
        while (!currentLine.isEmpty()) {
            data.add(currentLine);
            currentLine = new ArrayList(reader.getNextLine());
            countLines++;
        }

        // Creating and pre-initialising the ranking with all 0s
        List<List<Integer> > ranks = new ArrayList();
        for (int i = 0; i < data.size(); i++) {
            ranks.add(new ArrayList());
            for (int j = 0; j < data.get(i).size(); j++) ranks.get(i).add(0);
        }

        // Calculating the index by forming a tuple with the each value and its original position in the column, then sorting each column by the value and mapping the order into the ranks table.
        for(int i = 0; i < data.get(0).size(); i++) {
            List<DataWithIndex> l = new ArrayList();
            for (int j = 0; j < data.size(); j++) {
                l.add(new DataWithIndex(new Double(data.get(j).get(i)), j));
            }
            Collections.sort(l);
            int order = 1;
            double oldVal = l.get(0).data;
            ranks.get(l.get(0).index).set(i, order);
            for (int j = 1; j < l.size(); j++) {
                // if the values are the same, then the ranking value will be the same.
                if (l.get(j).data != oldVal) {
                    order++;
                    oldVal = l.get(j).data;
                }
                ranks.get(l.get(j).index).set(i, order);

            }
        }

        // creates the ktsp class, initialising all the data needed. This class will be responsible of running
        Ktsp ktsp = new Ktsp(ranks, normalIds, tumorIds, reader.getGeneNames(), writer);
        // Running different modes of the algorithm, and checking some errors. Options that are not valid for the mode are
        // ignored and a warning is printed.
        if (predictionOnly) {
            // Reading input model from file.
            ModelFileReader mreader = new ModelFileReader(modfile, reader.getGeneNames());
            if (mreader.getModel().pairs.size()%2 == 0) {
                System.err.println("The number of pairs in the model input file  specified by the -p option should be odd");
                System.exit(1);
            }
            if (iterations != -1) System.err.println("Warning: Ignoring -n option when -p option is present");
            if (kmax != -1) System.err.println("Warning: Ignoring -k option when -p option is present");
            if (show != -1) System.err.println("Warning: Ignoring -s option when -p option is present");
            if (isoforms) System.err.println("Warning: Ignoring -i option when -p option is present");
            if (randomiseLabels) System.err.println("Warning: Ignoring -l option when -p option is present");
            if (randomModel) System.err.println("Warning: Ignoring -r option when -p option is present");

            // running ktsp in prediction only mode with the model read from the file
            ktsp.testModel(mreader.getModel(), showPredictionDetails);
        }
        else if (randomModel) {
            if (iterations != -1) System.err.println("Warning: Ignoring -n option when -r option is present");
            if (kmax != -1) System.err.println("Warning: Ignoring -k option when -r option is present");
            if (show != -1) System.err.println("Warning: Ignoring -s option when -r option is present");
            if (randomiseLabels) System.err.println("Warning: Ignoring -l option when -r option is present");
            // creating a model with random pairs
            KtspPredictionModel model = new KtspPredictionModel();
            // ensuring that the requirements specific for isoform data are met by the randomomly selected pairs
            if (isoforms) {
                List<String> idGene = reader.getGeneNames();
                for (int i = 0; i < nrandom; i++) {
                    int first = random.nextInt(reader.getGeneNames().size());
                    String currentGeneId = idGene.get(first).substring(0, idGene.get(first).indexOf(","));
                    int current = first;
                    int j = current;
                    while (j >= 0 && idGene.get(j).substring(0, idGene.get(first).indexOf(",")).equals(currentGeneId)) j--;
                    int start = j + 1;
                    j = current;
                    while (j < idGene.size() && idGene.get(j).substring(0, idGene.get(first).indexOf(",")).equals(currentGeneId)) j++;
                    int end = j - 1;
                    if (start != end) {
                        int second = start + random.nextInt(end - start + 1);
                        while (first == second) second = start + random.nextInt(end - start + 1);
                        model.pairs.add(new PairRankInfo(first, second, -1, -1));
                    }
                    else {
                        i--;
                    }
                }
            }
            // for genes, there are no restrictions on the pairs selected
            else {
                for (int i = 0; i < nrandom; i++) {
                    int first = random.nextInt(reader.getGeneNames().size());
                    int second = random.nextInt(reader.getGeneNames().size());
                    while (first == second) second = random.nextInt(reader.getGeneNames().size());
                    model.pairs.add(new PairRankInfo(first, second, -1, -1));
                }
            }
            // running ktsp prediction only mode with the randomly generated model
            ktsp.testModel(model, showPredictionDetails);
        }
        else if (randomiseLabels) {
            if (iterations != -1) System.err.println("Warning: Ignoring -n option when -l option is present");
            if (kmax != -1) System.err.println("Warning: Ignoring -k option when -l option is present");
            if (show != -1) System.err.println("Warning: Ignoring -s option when -l option is present");
            if (showPredictionDetails) System.err.println("Warning: Ignoring -d option whithout -p or -r options");
            // running ktsp a given number of times, while shuffling the sample labels on each iteration
            ktsp.runKtspWithRandomisedLabels(nrandomiseLabels, isoforms, random);
        }
        else {
            if (iterations == -1) iterations = 10;
            if (kmax == -1) kmax = 10;
            if (show == -1) show = 10;
            if (showPredictionDetails) System.err.println("Warning: Ignoring -d option whithout -p or -r options");
            // running the standard mode of ktsp or the isoform variant of the standard mode
            KtspPredictionModel model = ktsp.runKtspWithCrossvvalidation(kmax, iterations, show, isoforms);
            List<String> geneIds = reader.getGeneNames();
            // printing the model selected by the algorithm
            for (int i = 0; i < model.pairs.size(); i++) {
                writer.printFinalModelPair(geneIds.get(model.pairs.get(i).firstIndex), geneIds.get(model.pairs.get(i).secondIndex));
            }
        }
        // closing the writing channel of the ouptut file.
        writer.closeFile();
    }

    // this auxiliary function prints the help.
    private static void printHelp() {
        System.out.println("iso-kTSP - A modified kTSP algorithm for alternative splicing isoform analysis");
        System.out.println();
        System.out.println("This program runs the kTSP classification algorithm giving both intermediate");
        System.out.println("results and a final prediction model. There are options to perform the analysis on");
        System.out.println("alternative splicing isoforms and to test an already defined kTSP model (of genes or isoforms)");
        System.out.println("against a dataset. There are options for permutation analysis and randomization.");
        System.out.println();
        System.out.println("The command format is:");
        System.out.println("java -jar iso-kTSP.jar <input_file> [options]");
        System.out.println();
        System.out.println("Options may be included in any order, but if an option requires specific parameters,");
        System.out.println("they should follow the option and be separated by spaces. Options should be written");
        System.out.println("separately, i.e. no grouping of options as in linux-unix style.");
        System.out.println();
        System.out.println("- Mandatory argument");
        System.out.println("    input dataset filepath: the name or the path of the file with the input dataset.");
        System.out.println();
        System.out.println("- Options");
        System.out.println("   -h : prints this help. (should be the only argument)");
        System.out.println("   -i : (no parameter) the program will run in the mode for alternative splicing isoforms.");
        System.out.println("        When not included, the standard algorithm for genes is run.");
        System.out.println("        Note that this option is not required when running the program with a defined model");
        System.out.println("        with the -m option, even if the model uses isoforms and not genes. For all the other");
        System.out.println("        modes this option should be included if the program runs over isoform data.");
        System.out.println("   -o : followed by a filename, defines the output file name. When not included,");
        System.out.println("        the output name will be the same as the input adding \"_output\" at the end or");
        System.out.println("        \"_output_mod\", \"_output_rand\" and \"_output_randlabels\" when run with -m, -r and -l");
        System.out.println("        options, respectively.");
        System.out.println("   -n : followed by an integer, defines the number of iterations for the cross-validation");
        System.out.println("        step of the algorithm (and indirectly determines the size of the test portion");
        System.out.println("        for the cross-validation). Default is 10. The integer specified by this option");
        System.out.println("        should be positive and not greater than the size of the sample set for the");
        System.out.println("        least represented class.");
        System.out.println("   -k : followed by an integer, defines the maximum value of the variable k, k_max, for the");
        System.out.println("        kTSP algorithm. Default is 10. Note that the algorithm uses only odd values for");
        System.out.println("        accuracy testing but this option accepts an even number, since it denotes a");
        System.out.println("        maximum (e.g. defining the maximum k as 9 or 10 has exactly the same effect).");
        System.out.println("   -s : followed by an integer, defines the number of the best (gene or isoform) pairs");
        System.out.println("        displayed at the final step of the algorithm with their single-pair performances.");
        System.out.println("        This number does not affect the number of pairs k_opt proposed from the cross-validation");
        System.out.println("        and can be greater than the defined maximum k_max. Default is 10.");
        System.out.println("   -c : followed by two strings separated by space, which define the suffixes in the sample");
        System.out.println("        names used to separate between the two classes used for classification. Default is N and T.");
        System.out.println("   -p : followed by the name or path of the file defining a (gene or isoform) kTSP model,");
        System.out.println("        which is tested in a prediction-only mode against the provided dataset.");
        System.out.println("        See below for details on the format of the model file.");
        System.out.println("   -d : (no parameter) this option can be used with -p or -r options to report");
        System.out.println("        for each tested sample the number of correct and incorrect votes.");
        System.out.println("   -l : followed by an integer, defines the number of iterations for the permutation analysis on the labels.");
        System.out.println("        The program will perform the final selection step of the algorithm over this number of iterations,");
        System.out.println("        each time with a random permutation of the sample labels. For each permutation the best (gene or isoform)");
        System.out.println("        pair and corresponding single-pair performance is reported.");
        System.out.println("   -r : followed by an integer, defines the number of random (gene or isoform) pairs to be tested in prediction-only mode");
        System.out.println("        agains the provided dataset. The integer specified should be odd and positive.");
        System.out.println("   --seed :");
        System.out.println("        followed by an integer, defines the seed that is used for every random step in the algorithm.");
        System.out.println("        If not present, the seed will be selected as defined in the java class Random when no seed is specified.");
        System.out.println();
        System.out.println("Ignored options:");
        System.out.println("      Some options specify different modes in which the program runs, and some options have no effect on specific modes.");
        System.out.println("      If the user specifies an option that is not needed for the selected mode, this option will be ignored and the");
        System.out.println("      program will continue running normally after printing a warning. Because it is impossible to run the program in");
        System.out.println("      different modes at the same time, some modes will take priority over others, and the corresponding options will be");
        System.out.println("      ignored. Here is a list of the options that are ignored for each mode.");
        System.out.println("      -p mode ignores options -n, -k, -s, -i, -l and -r.");
        System.out.println("      -r mode ignores options -n, -k, -s and -l.");
        System.out.println("      -l mode ignores options -n, -k, -s and -d.");
        System.out.println("      Normal mode (when none of the previous modes is specified) ignores option -d,");
        System.out.println();
        System.out.println("Examples of calls:");
        System.out.println("      java -jar iso-kTSP.jar gene_seq.txt");
        System.out.println("      java -jar iso-kTSP.jar -o out_iso_analysis.txt -i -k 12 iso_data.tab");
        System.out.println("      java -jar iso-kTSP.jar -o out_iso_analysis.txt /home/user/iso_data.tab -c tumor normal -i -n 15 -s 40 -k 4");
        System.out.println();
        System.out.println("Input format:");
        System.out.println("      The expected format for the input dataset is a tab-separated plain text file (with any extension),");
        System.out.println("      where the first row contains the sample labels with suffixes to differentiate between samples");
        System.out.println("      belonging to different classes, not necessarily paired. Subsequent lines contain the \"gene_id\",");
        System.out.println("      or \"gene_id,isoform_id\" for isoforms, in the first column followed by the sample data values");
        System.out.println("      (in any numerical format that java can parse), in the same order as in the first row.");
        System.out.println();
        System.out.println("      The expected format for the model input file (when using the option -m) is a plain text file");
        System.out.println("      (with any extension) that should contain in each line a pair of \"gene_id\", or of \"gene_id,isoform_id\"");
        System.out.println("      for isoforms, separated by a single tab. The number of pairs in the file must be odd.");
        System.out.println();
        System.out.println("Output format:");
        System.out.println("       The output has multiple lines with different formats. In each line, it is first reported the iteration");
        System.out.println("       within the cross-validation (or \"final\" if related to the last steps of the algorithm, after the cross-validation)");
        System.out.println("       is given, and then the type of result and the result are given:");
        System.out.println();
        System.out.println("       iteration=i kmax_pair         : at each iteration of the cross-validation this lines provide the k_max best scoring pairs");
        System.out.println("                                       selected in the learning part of that iteration with its scores in ranking order");
        System.out.println("       iteration=i k_performance     : at each iteration of the cross-validation this lines provide the results");
        System.out.println("                                       of the prediction using the top k pairs listed before, where k is odd and smaller than k_max.");
        System.out.println("                                       The performance is provided in terms of the number of true and false predictions:");
        System.out.println("                                       \"Tclass1\" (true class1), where class1 is class 1 label, means that a sample was predicted");
        System.out.println("                                       to be class 1 and the prediction was right, whereas \"Fclass1\", means a sample was predicted");
        System.out.println("                                       to be class1 but the prediction was wrong; and similarly for class 2.");
        System.out.println("       final k_average_performance   : After the cross-validation, these lines provide the average performance for each tested k");
        System.out.println("                                       over all iterations, and k_opt is defined to be the smallest k < k_max and k odd");
        System.out.println("                                       that has the best average performance is selected for the final model. The performance");
        System.out.println("                                       is calculated as the overall success rate (= the proportion of correct");
        System.out.println("                                       predictions (Tclass1 + Tclass2) over all the predictions made).");
        System.out.println("       final single_pair_performance : after selecting the best k from the cross-validation, the (-s) pairs are re-scored using all");
        System.out.println("                                       the input data and the best k_opt pairs are selected for the final model. The performance of");
        System.out.println("                                       each single pair is provided together with the Information Gain and the scores used for selection.");
        System.out.println("       final model_pair              : the pairs that the algorithm chooses for the final model. Basically, the k top pairs from the");
        System.out.println("                                       previous list (with the k selected from the cross-validation).");
        System.out.println();
        System.out.println("       For the prediction only mode (options -p and -r), the output is the single pair performance of each of the pairs in");
        System.out.println("       the model and the overall performance of the complete prediction model.");
        System.out.println();
        System.out.println("       When -d option is present, the output will also contain the specific details of the prediction of each sample,");
        System.out.println("       giving the number of correct and incorrect votes, that is, the number of pairs in the model that contribute to predict");
        System.out.println("       correctly or incorrectly each sample.");
        System.out.println();
        System.out.println("       For the permutation mode, at each iteration only the best scoring pair in that iteration is reported with its single_pair_performance.");
        System.out.println();
        System.out.println("       The semantics of a isoform-pair (or gene-pair) rule is that if the first element is lower than the second in the ranking of expression, the prediction is class1,");
        System.out.println("       and in any other case the prediction is class2.");

        
    }

}

