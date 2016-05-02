package ktsp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 *
 * @author michal
 *
 * This class has all the data to do the main steps of the ktsp algorithm except the construction of the
 * ranking table, which is already given to the class. It also has the methods to run the algorithm in its
 * different modes.
 */
public class Ktsp {
    // The ranking table that has for each sample (column) the order of each gene or isoform (rows) according
    // to the value of the original input file
    private List<List<Integer> > ranks = null;
    // A list that maps the positions of the columns corresponding to normal or class1 samples
    private List<Integer> normal = null;
    // A list that maps the positions of the columns corresponding to tumor or class2 samples
    private List<Integer> tumor = null;
    // A list with the original gene or isoform identifiers corresponding to the rows of the table.
    private List<String> idGene = null;
    // The writer class responsible of managing the output of the program.
    private FileWriter writer = null;

    // Constructor that simply initialises all the lists with the given arguments.
    Ktsp(List<List<Integer> > r, List<Integer> n, List<Integer> t, List<String> g, FileWriter w) {
        ranks = r;
        normal = n;
        tumor = t;
        idGene = g;
        writer = w;
    }

    // This method is the standard mode of the algorithm, with the crossvalidation step to identify the best k
    // and a final step to select the pairs for the final model.
    KtspPredictionModel runKtspWithCrossvvalidation(int kmax, int iterations, int show, boolean isoformas) {
        //Calculating the sizes of the test blocks for the iterations in the crossvalidation.for each of the classes of samples
        int testBlockSizeNormal = normal.size() / iterations;
        int testBlockSizeTumor = tumor.size() / iterations;
        // Variables that mark the start of the testing blocks and that will increment on each iteration of the crossvalidation.
        int currentStartNormalTest = 0;
        int currentStartTumorTest = 0;
        // List to store the results of each of the crossvalidation iterations.
        List<KtspPredictionModel> partialResults = new ArrayList();

        // Running the crossvalidation iterations calling to the method that runs individual iterations or the isoform specific variation.
        // On each iteration, the test blocks are moved by the block sizes and in the last iteration, we add the remaining samples to the test blocks.
        for (int i = 0; i < iterations; i++) {
            writer.nextIteration();
            if (i == iterations - 1) {
                if (isoformas) {
                    partialResults.add(runSingleKtspIso(kmax, currentStartNormalTest, normal.size() - 1, currentStartTumorTest, tumor.size() - 1));
                }
                else {
                    partialResults.add(runSingleKtsp(kmax, currentStartNormalTest, normal.size() - 1, currentStartTumorTest, tumor.size() - 1));
                }
            }
            else {
                if (isoformas) {
                    partialResults.add(runSingleKtspIso(kmax, currentStartNormalTest, currentStartNormalTest + testBlockSizeNormal - 1, currentStartTumorTest, currentStartTumorTest + testBlockSizeTumor - 1));
                }
                else {
                    partialResults.add(runSingleKtsp(kmax, currentStartNormalTest, currentStartNormalTest + testBlockSizeNormal - 1, currentStartTumorTest, currentStartTumorTest + testBlockSizeTumor - 1));
                }
                
            }
            currentStartNormalTest += testBlockSizeNormal;
            currentStartTumorTest += testBlockSizeTumor;
        }

        // Calculating the average performance of each k over all the iterations in order to select the best k.
        int bestK = -1;
        double bestAverage = 0;
        int currentK = 1;
        for (int i = 0; i < partialResults.get(0).testingPerformance.size(); i++) {
            double accumulatedPerformance = 0;
            for (int j = 0; j < partialResults.size(); j++) {
                accumulatedPerformance += partialResults.get(j).testingPerformance.get(i);
            }
            double currentAverage = accumulatedPerformance / (double)partialResults.size();

            if (currentAverage > bestAverage) {
                bestAverage = currentAverage;
                bestK = currentK;
            }
            writer.printKAveragePerformance(currentK, currentAverage);
            currentK += 2;
        }
        // Running a special selectionOnly step with the bestK and returning the resulting model as the final result.
        KtspPredictionModel finalModel;
        if (!isoformas) finalModel = fixedKSelection(bestK, show);
        else finalModel = fixedKSelectionIso(bestK, show);
        return finalModel;
    }

    // This method runs the final selection step of the ktsp algorithm without crossvalidation and with k = 1 several times,
    // each tiime shuffling the columns between the two classes.
    void runKtspWithRandomisedLabels(int iterations, boolean isoform, Random random) {
        int sizeNormal = normal.size();
        int sizeTumor = tumor.size();
        for (int i = 0; i < iterations; i++) {
            normal.clear();
            tumor.clear();
            for (int j = 0; j < sizeNormal + sizeTumor; j++) {
                if (normal.size() < sizeNormal && tumor.size() < sizeTumor) {
                    if (random.nextInt(sizeNormal + sizeTumor) < sizeNormal - 1) normal.add(j);
                    else tumor.add(j);
                }
                else if (normal.size() < sizeNormal) normal.add(j);
                else tumor.add(j);
            }
            KtspPredictionModel result = null;
            if (isoform) result = fixedKSelectionIso(1, 1);
            else result = fixedKSelection(1, 1);
            writer.printRandomLabelResult(result.pairs.get(0).score, result.pairs.get(0).rankScore);
            writer.nextIteration();
        }
    }

    // This method runs one step of the ktsp algorithm, performing learning and testing, given the maximum k and the testing blocks.
    KtspPredictionModel runSingleKtsp(int kmax, int tStartNormal, int tEndNormal, int tStartTumor, int tEndTumor) {
        // the model containing the result of the learning and testing
        KtspPredictionModel result = new KtspPredictionModel();
        // the TreeSet structure provides an efficient way to keep an ordered set. We will use it to keep the valid pairs
        // ordered by their scores
        TreeSet<PairRankInfo> pairList = new TreeSet();
        // the learning step: for each possible pair of genes we compute the scores and we add the pair to the TreeSet
        for (int i = 0; i < ranks.size(); i++) {
            for (int j = 0; j < ranks.size(); j++) {
                if (i != j) {
                    int count = 0;
                    int cumRankDiff = 0;
                    for (int n = 0; n < normal.size(); n++) {
                        if (n < tStartNormal || n > tEndNormal) {
                            cumRankDiff += (ranks.get(i).get(normal.get(n)) - ranks.get(j).get(normal.get(n)));
                            if (ranks.get(i).get(normal.get(n)) < ranks.get(j).get(normal.get(n)) ) {
                                count++;
                            }
                        }

                    }
                    double probNormal = (double) count / (double)(normal.size() - (tEndNormal - tStartNormal + 1));
                    double AvRankDiffNormal = (double)cumRankDiff / (double)(normal.size() - (tEndNormal - tStartNormal + 1));
                    count = 0;
                    cumRankDiff = 0;
                    for (int n = 0; n < tumor.size(); n++) {
                        if (n < tStartTumor || n > tEndTumor) {
                            cumRankDiff += (ranks.get(i).get(tumor.get(n)) - ranks.get(j).get(tumor.get(n)));

                            if (ranks.get(i).get(tumor.get(n)) > ranks.get(j).get(tumor.get(n)) ) {
                                count++;
                            }
                        }
                    }

                    double probTumor = (double) count / (double) (tumor.size() - (tEndTumor - tStartTumor + 1));
                    double AvRankDiffTumor = (double)cumRankDiff / (double)(tumor.size() - (tEndTumor - tStartTumor + 1));
                    double pDiff = probNormal + probTumor;
                    double rScore = Math.abs(AvRankDiffTumor - AvRankDiffNormal);
                    // we only store the best (kmax * rows) pairs, because we are never goning to use more anyway
                    if (pairList.size() < ranks.size() * kmax) {
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }
                    else if (pairList.first().score < pDiff || (pairList.first().score == pDiff && pairList.first().rankScore < rScore)) {
                        pairList.pollFirst();
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }
                }
            }
        }
        // we store which genes we've already used because we want each gene to appear once
        Set<Integer> usedIndexes = new HashSet();
        // Until we reach kmax or there are no more pairs left, we add the best pairs to the model if none
        // of its genes have already been added
        while (result.pairs.size() < kmax && !pairList.isEmpty()) {
            PairRankInfo candidate = pairList.pollLast();
            if (!usedIndexes.contains(candidate.firstIndex) && !usedIndexes.contains(candidate.secondIndex)) {
                result.pairs.add(candidate);
                usedIndexes.add(candidate.firstIndex);
                usedIndexes.add(candidate.secondIndex);
                // we print the info of the kmax pairs selected in the learning
                writer.printIterationKmaxPair(idGene.get(candidate.firstIndex), idGene.get(candidate.secondIndex), candidate.score, candidate.rankScore);
                
            }
            
        }
        // check if we have run out of genes before reaching kmax
        if (result.pairs.size() < kmax) {

            kmax = result.pairs.size();
        }
        // the testing step: for each odd k until kmax we take the k pairs from the model of the learning step and check how
        // many correct and incorrect predictions the model with that k makes in the testing samples
        for (int k = 1; k <= kmax; k += 2) {
            int normalCorrect = 0;
            int normalIncorrect = 0;
            for (int i = tStartNormal; i <= tEndNormal; i++) {
                int normalVotes = 0;
                int tumorVotes = 0;
                for (int j = 0; j < k; j++) {
                    if (ranks.get(result.pairs.get(j).firstIndex).get(normal.get(i)) < ranks.get(result.pairs.get(j).secondIndex).get(normal.get(i)) ) normalVotes++;
                    else tumorVotes++;
                }
                if (normalVotes > tumorVotes) normalCorrect++;
                else normalIncorrect++;
            }
            int tumorCorrect = 0;
            int tumorIncorrect = 0;
            for (int i = tStartTumor; i <= tEndTumor; i++) {
                int normalVotes = 0;
                int tumorVotes = 0;
                for (int j = 0; j < k; j++) {
                    if (ranks.get(result.pairs.get(j).firstIndex).get(tumor.get(i)) <= ranks.get(result.pairs.get(j).secondIndex).get(tumor.get(i)) ) normalVotes++;
                    else tumorVotes++;
                }
                if (normalVotes < tumorVotes) tumorCorrect++;
                else tumorIncorrect++;
            }
            // we print the performance to the output file
            writer.printIterationKPerformance(k, normalCorrect, tumorCorrect, tumorIncorrect, normalIncorrect);

            // we add the a unique accuracy measure to the info of the model
            result.testingPerformance.add((double)(normalCorrect + tumorCorrect)/(double)(normalCorrect + tumorCorrect + normalIncorrect + tumorIncorrect));
        }
        return result;
    }

    // similar to the runSingleKtsp method, but with an adaptation for isoforms that checks the valid isoform
    // pairs are from the same gene
    KtspPredictionModel runSingleKtspIso(int kmax, int tStartNormal, int tEndNormal, int tStartTumor, int tEndTumor) {
        // the model containing the result of the learning and testing
        KtspPredictionModel result = new KtspPredictionModel();
        // the TreeSet structure provides an efficient way to keep an ordered set. We will use it to keep the valid pairs
        // ordered by their scores
        TreeSet<PairRankInfo> pairList = new TreeSet();
        // we keep track of the gene of the current isoform we are pairing to avoid pairing with isoforms from other genes
        String currentGeneId = idGene.get(0).substring(0, idGene.get(0).indexOf(","));
        int currentGeneStartPosition = 0;
        // learning step: for every pair of isoforms of the same gene, we compute the scores and we add the pair to the TreeSet
        for (int i = 0; i < ranks.size(); i++) {
            // checking if we have moved on to an isoform of a different gene
            if (!currentGeneId.equals(idGene.get(i).substring(0, idGene.get(i).indexOf(",")))) {
                currentGeneId = idGene.get(i).substring(0, idGene.get(i).indexOf(","));
                currentGeneStartPosition = i;
            }
            int j = currentGeneStartPosition;
            // we only loop over the isoforms that share the same geneId as the current pairing isoform
            boolean notEndGene = true;
            while (notEndGene) {
                if (i != j) {
                    int count = 0;
                    int cumRankDiff = 0;
                    for (int n = 0; n < normal.size(); n++) {
                        if (n < tStartNormal || n > tEndNormal) {
                            cumRankDiff += (ranks.get(i).get(normal.get(n)) - ranks.get(j).get(normal.get(n)));
                            if (ranks.get(i).get(normal.get(n)) < ranks.get(j).get(normal.get(n)) ) {
                                count++;
                            }
                        }

                    }
                    double probNormal = (double) count / (double)(normal.size() - (tEndNormal - tStartNormal + 1));
                    double AvRankDiffNormal = (double)cumRankDiff / (double)(normal.size() - (tEndNormal - tStartNormal + 1));
                    count = 0;
                    cumRankDiff = 0;
                    for (int n = 0; n < tumor.size(); n++) {
                        if (n < tStartTumor || n > tEndTumor) {
                            cumRankDiff += (ranks.get(i).get(tumor.get(n)) - ranks.get(j).get(tumor.get(n)));

                            if (ranks.get(i).get(tumor.get(n)) > ranks.get(j).get(tumor.get(n)) ) {
                                count++;
                            }
                        }
                    }

                    double probTumor = (double) count / (double) (tumor.size() - (tEndTumor - tStartTumor + 1));
                    double AvRankDiffTumor = (double)cumRankDiff / (double)(tumor.size() - (tEndTumor - tStartTumor + 1));
                    double pDiff = probNormal + probTumor;
                    double rScore = Math.abs(AvRankDiffTumor - AvRankDiffNormal);

                    // we only store the best (kmax * rows) pairs, because we are never goning to use more anyway
                    if (pairList.size() < ranks.size() * kmax) {
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }
                    else if (pairList.first().score < pDiff || (pairList.first().score == pDiff && pairList.first().rankScore < rScore)) {
                        pairList.pollFirst();
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }
                }
                j++;
                if (j == ranks.size()) {
                    notEndGene = false;
                }
                else if (!currentGeneId.equals(idGene.get(j).substring(0, idGene.get(j).indexOf(",")))) {
                    notEndGene = false;
                }
            }
        }

        // we store which genes and isoforms we've already used because we want each gene and isofrom to appear once
        Set<Integer> usedIndexes = new HashSet();
        Set<String> usedGenes = new HashSet();
        // Until we reach kmax or there are no more pairs left, we add the best pairs to the model if none
        // of its genes and isoforms have already been added
        while (result.pairs.size() < kmax && !pairList.isEmpty()) {
            PairRankInfo candidate = pairList.pollLast();
            if (!usedIndexes.contains(candidate.firstIndex) && !usedIndexes.contains(candidate.secondIndex) && !usedGenes.contains(idGene.get(candidate.firstIndex).substring(0, idGene.get(candidate.firstIndex).indexOf(",")))) {
                result.pairs.add(candidate);
                usedIndexes.add(candidate.firstIndex);
                usedIndexes.add(candidate.secondIndex);
                usedGenes.add(idGene.get(candidate.firstIndex).substring(0, idGene.get(candidate.firstIndex).indexOf(",")));
                // we print the info of the kmax pairs selected in the learning
                writer.printIterationKmaxPair(idGene.get(candidate.firstIndex), idGene.get(candidate.secondIndex), candidate.score, candidate.rankScore);
            }

        }
        // check if we have run out of genes before reaching kmax
        if (result.pairs.size() < kmax) {
            kmax = result.pairs.size();
        }
        // the testing step: for each odd k until kmax we take the k pairs from the model of the learning step and check how
        // many correct and incorrect predictions the model with that k makes in the testing samples
        for (int k = 1; k <= kmax; k += 2) {
            int normalCorrect = 0;
            int normalIncorrect = 0;
            for (int i = tStartNormal; i <= tEndNormal; i++) {
                int normalVotes = 0;
                int tumorVotes = 0;
                for (int j = 0; j < k; j++) {
                    if (ranks.get(result.pairs.get(j).firstIndex).get(normal.get(i)) < ranks.get(result.pairs.get(j).secondIndex).get(normal.get(i)) ) normalVotes++;
                    else tumorVotes++;
                }
                if (normalVotes > tumorVotes) normalCorrect++;
                else normalIncorrect++;
            }
            int tumorCorrect = 0;
            int tumorIncorrect = 0;
            for (int i = tStartTumor; i <= tEndTumor; i++) {
                int normalVotes = 0;
                int tumorVotes = 0;
                for (int j = 0; j < k; j++) {
                    if (ranks.get(result.pairs.get(j).firstIndex).get(tumor.get(i)) <= ranks.get(result.pairs.get(j).secondIndex).get(tumor.get(i)) ) normalVotes++;
                    else tumorVotes++;
                }
                if (normalVotes < tumorVotes) tumorCorrect++;
                else tumorIncorrect++;
            }
            // we print the performance to the output file
            writer.printIterationKPerformance(k, normalCorrect, tumorCorrect, tumorIncorrect, normalIncorrect);

            // we add a unique accuracy measure to the info of the model
            result.testingPerformance.add((double)(normalCorrect + tumorCorrect)/(double)(normalCorrect + tumorCorrect + normalIncorrect + tumorIncorrect));
        }
        return result;
    }

    // this method porforms only the learning or selection step of the algorithm over all the dataset, given a fixed k and also
    // may print the single pair performances of the selected pairs and more pairs if a greater show argument is specified
    KtspPredictionModel fixedKSelection(int k, int show) {
        // the model containing the result of the learning and testing
        KtspPredictionModel result = new KtspPredictionModel();
        // the TreeSet structure provides an efficient way to keep an ordered set. We will use it to keep the valid pairs
        // ordered by their scores
        TreeSet<PairRankInfo> pairList = new TreeSet();
        // the learning step: for each possible pair of genes we compute the scores and we add the pair to the TreeSet
        for (int i = 0; i < ranks.size(); i++) {
            for (int j = 0; j < ranks.size(); j++) {
                if (i != j) {
                    int count = 0;
                    int cumRankDiff = 0;
                    for (int n = 0; n < normal.size(); n++) {
                        cumRankDiff += (ranks.get(i).get(normal.get(n)) - ranks.get(j).get(normal.get(n)));
                        if (ranks.get(i).get(normal.get(n)) < ranks.get(j).get(normal.get(n)) ) {
                            count++;
                        }

                    }
                    double probNormal = (double) count / (double)(normal.size());
                    double AvRankDiffNormal = (double)cumRankDiff / (double)(normal.size());
                    count = 0;
                    cumRankDiff = 0;
                    for (int n = 0; n < tumor.size(); n++) {
                        cumRankDiff += (ranks.get(i).get(tumor.get(n)) - ranks.get(j).get(tumor.get(n)));
                        if (ranks.get(i).get(tumor.get(n)) > ranks.get(j).get(tumor.get(n)) ) {
                            count++;
                        }
                    }

                    double probTumor = (double) count / (double) (tumor.size());
                    double AvRankDiffTumor = (double)cumRankDiff / (double)(tumor.size());
                    double pDiff = probNormal + probTumor;
                    double rScore = Math.abs(AvRankDiffTumor - AvRankDiffNormal);
                    // we only store the best (kmax * rows) pairs, because we are never goning to use more anyway
                    if (pairList.size() < ranks.size() * k) {
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }
                    else if (pairList.first().score < pDiff || (pairList.first().score == pDiff && pairList.first().rankScore < rScore)) {
                        pairList.pollFirst();
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }

                }
            }
        }
        // we store which genes we've already used because we want each gene to appear once
        Set<Integer> usedIndexes = new HashSet();
        // we count both how many pairs we have added to the model and how many pairs we have printed with their
        // single pair performance
        int shownPairs = 0;
        int addedPairs = 0;
        // we print the specified number of pairs with their single pair performance and add them to the model if we haven't
        // reached the specified k
        while (shownPairs < show && !pairList.isEmpty()) {
            PairRankInfo candidate = pairList.pollLast();
            if (!usedIndexes.contains(candidate.firstIndex) && !usedIndexes.contains(candidate.secondIndex)) {
                if (result.pairs.size() < k) {
                    result.pairs.add(candidate);
                    addedPairs++;
                }
                testWithSinglePair(candidate);
                usedIndexes.add(candidate.firstIndex);
                usedIndexes.add(candidate.secondIndex);
                shownPairs++;
            }

        }
        // in case there are more pairs to be added to the model printing all the single pair performances, we continue adding
        // the pairs
        while (addedPairs < k && !pairList.isEmpty()) {
            PairRankInfo candidate = pairList.pollLast();
            if (!usedIndexes.contains(candidate.firstIndex) && !usedIndexes.contains(candidate.secondIndex)) {
                result.pairs.add(candidate);
                addedPairs++;
                usedIndexes.add(candidate.firstIndex);
                usedIndexes.add(candidate.secondIndex);
            }
        }
        return result;
    }

    // similar to the fixedKSelection method, but with an adaptation for isoforms that checks the valid isoform
    // pairs are from the same gene
    KtspPredictionModel fixedKSelectionIso(int k, int show) {
        // the model containing the result of the learning and testing
        KtspPredictionModel result = new KtspPredictionModel();
        // the TreeSet structure provides an efficient way to keep an ordered set. We will use it to keep the valid pairs
        // ordered by their scores
        TreeSet<PairRankInfo> pairList = new TreeSet();
        // we keep track of the gene of the current isoform we are pairing to avoid pairing with isoforms from other genes
        String currentGeneId = idGene.get(0).substring(0, idGene.get(0).indexOf(","));
        int currentGeneStartPosition = 0;
        // learning step: for every pair of isoforms of the same gene, we compute the scores and we add the pair to the TreeSet
        for (int i = 0; i < ranks.size(); i++) {
            // checking if we have moved on to an isoform of a different gene
            if (!currentGeneId.equals(idGene.get(i).substring(0, idGene.get(i).indexOf(",")))) {
                currentGeneId = idGene.get(i).substring(0, idGene.get(i).indexOf(","));
                currentGeneStartPosition = i;
            }
            int j = currentGeneStartPosition;
            boolean notEndGene = true;
            // we only loop over the isoforms that share the same geneId as the current pairing isoform
            while (notEndGene) {
                if (i != j) {
                    int count = 0;
                    int cumRankDiff = 0;
                    for (int n = 0; n < normal.size(); n++) {
                        cumRankDiff += (ranks.get(i).get(normal.get(n)) - ranks.get(j).get(normal.get(n)));

                        if (ranks.get(i).get(normal.get(n)) < ranks.get(j).get(normal.get(n)) ) {
                            count++;
                        }

                    }
                    double probNormal = (double) count / (double)(normal.size());
                    double AvRankDiffNormal = (double)cumRankDiff / (double)(normal.size());
                    count = 0;
                    cumRankDiff = 0;
                    for (int n = 0; n < tumor.size(); n++) {
                        cumRankDiff += (ranks.get(i).get(tumor.get(n)) - ranks.get(j).get(tumor.get(n)));
                        if (ranks.get(i).get(tumor.get(n)) > ranks.get(j).get(tumor.get(n)) ) {
                            count++;
                        }
                    }
                    double probTumor = (double) count / (double) (tumor.size());
                    double AvRankDiffTumor = (double)cumRankDiff / (double)(tumor.size());
                    double pDiff = probNormal + probTumor;
                    double rScore = Math.abs(AvRankDiffTumor - AvRankDiffNormal);

                    // we only store the best (kmax * rows) pairs, because we are never goning to use more anyway
                    if (pairList.size() < ranks.size() * k) {
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }
                    else if (pairList.first().score < pDiff || (pairList.first().score == pDiff && pairList.first().rankScore < rScore)) {
                        pairList.pollFirst();
                        pairList.add(new PairRankInfo(i, j, pDiff, rScore));
                    }

                }
                j++;
                if (j == ranks.size()) {
                    notEndGene = false;
                }
                else if (!currentGeneId.equals(idGene.get(j).substring(0, idGene.get(j).indexOf(",")))) {
                    notEndGene = false;
                }
            }
        }
        // we store which genes and isoforms we've already used because we want each gene and isofrom to appear once
        Set<Integer> usedIndexes = new HashSet();
        Set<String> usedGenes = new HashSet();
        int shownPairs = 0;
        int addedPairs = 0;
        // we print the specified number of pairs with their single pair performance and add them to the model if we haven't
        // reached the specified k
        while (shownPairs < show && !pairList.isEmpty()) {
            PairRankInfo candidate = pairList.pollLast();
            if (!usedIndexes.contains(candidate.firstIndex) && !usedIndexes.contains(candidate.secondIndex) && !usedGenes.contains(idGene.get(candidate.firstIndex).substring(0, idGene.get(candidate.firstIndex).indexOf(",")))) {
                if (result.pairs.size() < k) {
                    result.pairs.add(candidate);
                    addedPairs++;
                }
                testWithSinglePair(candidate);
                usedIndexes.add(candidate.firstIndex);
                usedIndexes.add(candidate.secondIndex);
                usedGenes.add(idGene.get(candidate.firstIndex).substring(0, idGene.get(candidate.firstIndex).indexOf(",")));
                shownPairs++;
            }

        }
        // in case there are more pairs to be added to the model printing all the single pair performances, we continue adding
        // the pairs
        while (addedPairs < k && !pairList.isEmpty()) {
            PairRankInfo candidate = pairList.pollLast();
            if (!usedIndexes.contains(candidate.firstIndex) && !usedIndexes.contains(candidate.secondIndex) && !usedGenes.contains(idGene.get(candidate.firstIndex).substring(0, idGene.get(candidate.firstIndex).indexOf(",")))) {
                if (result.pairs.size() < k) {
                    result.pairs.add(candidate);
                    addedPairs++;
                    usedGenes.add(idGene.get(candidate.firstIndex).substring(0, idGene.get(candidate.firstIndex).indexOf(",")));
                    usedIndexes.add(candidate.firstIndex);
                    usedIndexes.add(candidate.secondIndex);
                }
            }
        }
        return result;
    }

    // method that performs testing over all samples with a single pair to compute and print the single pair performance and
    // other related info like the information gain.
    void testWithSinglePair(PairRankInfo pair) {
        // counting the correct and incorrect votes when predicting with the pair for all samples.
        int normalCorrect = 0;
        int normalIncorrect = 0;
        for (int i = 0; i < normal.size(); i++) {
            if (ranks.get(pair.firstIndex).get(normal.get(i)) < ranks.get(pair.secondIndex).get(normal.get(i))) normalCorrect++;
            else normalIncorrect++;
        }
        int tumorCorrect = 0;
        int tumorIncorrect = 0;
        for (int i = 0; i < tumor.size(); i++) {
            if (ranks.get(pair.firstIndex).get(tumor.get(i)) <= ranks.get(pair.secondIndex).get(tumor.get(i))) tumorIncorrect++;
            else tumorCorrect++;
        }
        // Computing the information gain. Note that we ensure that any operation that can give non-numeric results, should
        // be replaced by a 0.
        double TRel = ((double)tumor.size()/((double)tumor.size() + (double)normal.size()));
        double NRel = ((double)normal.size()/((double)tumor.size() + (double)normal.size()));
        double logTRel,logNRel;
        if (TRel == 0) logTRel = 0;
        else logTRel = Math.log(TRel)/Math.log(2);
        if (NRel == 0) logNRel = 0;
        else logNRel = Math.log(NRel)/Math.log(2);
        double aprioriEntropy = (-(TRel*logTRel)) - (NRel*logNRel);
        double TPRelT = (((double)normalIncorrect + (double)tumorCorrect)/((double)tumor.size() + (double)normal.size()));
        double NPRelT = (((double)normalCorrect + (double)tumorIncorrect)/((double)tumor.size() + (double)normal.size()));
        double TTRelPT, FTRelPT, TNRelPN, FNRelPN;
        if (tumorCorrect == 0 && normalIncorrect == 0) {
            TTRelPT = 0;
            FTRelPT = 0;
        }
        else {
            TTRelPT = ((double)tumorCorrect/((double)tumorCorrect + (double)normalIncorrect));
            FTRelPT = ((double)normalIncorrect/((double)tumorCorrect + (double)normalIncorrect));
        }
        if (tumorIncorrect == 0 && normalCorrect == 0) {
            TNRelPN = 0;
            FNRelPN = 0;
        }
        else {
            TNRelPN = ((double)normalCorrect/((double)normalCorrect + (double)tumorIncorrect));
            FNRelPN = ((double)tumorIncorrect/((double)normalCorrect + (double)tumorIncorrect));
        }
        double logTT, logFT, logTN, logFN;
        if (TTRelPT == 0) logTT = 0;
        else logTT = Math.log(TTRelPT)/Math.log(2);
        if (FTRelPT == 0) logFT = 0;
        else  logFT = Math.log(FTRelPT)/Math.log(2);
        if (TNRelPN == 0) logTN = 0;
        else  logTN = Math.log(TNRelPN)/Math.log(2);
        if (FNRelPN == 0) logFN = 0;
        else  logFN = Math.log(FNRelPN)/Math.log(2);

        double pairEntropy = TPRelT*(-(TTRelPT*logTT + FTRelPT*logFT)) + NPRelT*(-(TNRelPN*logTN + FNRelPN*logFN));
        double IG = aprioriEntropy - pairEntropy;
        // printing all the computed performance info
        writer.printSinglePairPerformance(idGene.get(pair.firstIndex), idGene.get(pair.secondIndex), normalCorrect, tumorCorrect, tumorIncorrect, normalIncorrect, IG, pair.score, pair.rankScore);
        
    }

    // this methos performs the prediction only mode of the program, which, given a model, performs the testing step only over
    // all the dataset. It prints a global performance result of the model and  if specified, it may also print details
    // of individual predictions on each sample
    void testModel(KtspPredictionModel model, boolean printDetails) {
        for (int i = 0; i < model.pairs.size(); i++) {
            testWithSinglePair(model.pairs.get(i));
        }
        int normalCorrect = 0;
        int normalIncorrect = 0;
        for (int i = 0; i < normal.size(); i++) {
            int normalVotes = 0;
            int tumorVotes = 0;
            for (int j = 0; j < model.pairs.size(); j++) {
                if (ranks.get(model.pairs.get(j).firstIndex).get(normal.get(i)) < ranks.get(model.pairs.get(j).secondIndex).get(normal.get(i)) ) normalVotes++;
                else tumorVotes++;
            }
            if (normalVotes > tumorVotes) normalCorrect++;
            else normalIncorrect++;
            if (printDetails) {
                writer.printPredictionDetails(normal.get(i), normalVotes, tumorVotes);
            }
        }
        int tumorCorrect = 0;
        int tumorIncorrect = 0;
        for (int i = 0; i < tumor.size(); i++) {
            int normalVotes = 0;
            int tumorVotes = 0;
            for (int j = 0; j < model.pairs.size(); j++) {
                if (ranks.get(model.pairs.get(j).firstIndex).get(tumor.get(i)) <= ranks.get(model.pairs.get(j).secondIndex).get(tumor.get(i)) ) normalVotes++;
                else tumorVotes++;
            }
            if (normalVotes < tumorVotes) tumorCorrect++;
            else tumorIncorrect++;
            if (printDetails) {
                writer.printPredictionDetails(tumor.get(i), tumorVotes, normalVotes);
            }
        }
        writer.printModelPerformance(normalCorrect, tumorCorrect, tumorIncorrect, normalIncorrect);


    }



}

