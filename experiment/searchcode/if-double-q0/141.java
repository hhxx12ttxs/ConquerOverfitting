package score;

import parsers.indexes.DocumentCollectionIndex;
import parsers.indexes.DocumentIndex;
import score.scores.QueryScore;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Calcule le Cosine Score selon l'algorithme vu en cours
 * (cf. "Recherche d'information - 3" slide 9 de Michel Beigbeder) --- ce n'est plus tout a fait vrai ---
 */
public class Cosine {

    public static final int SCORE_LTN = 1;
    public static final int SCORE_BM25 = 2;
    private DocumentCollectionIndex documentCollectionIndex;
    private ArrayList<DocumentIndex> queries;
    private LinkedHashMap<String, QueryScore> scores;
    private QueryScore tmpScore; // I did not use bufferScore cause it is to similar to bufferScores
    private int scoreType = Cosine.SCORE_LTN;
    private Double importanceSaturationTf = 0.0; // aka k
    private Double importanceNormalisationTaille = 0.0; // aka b

    public Cosine(DocumentCollectionIndex documentCollectionIndex, ArrayList<DocumentIndex> queries) {
        super();

        this.documentCollectionIndex = documentCollectionIndex;
        this.queries = queries;
    }

    public void computeLtnScore() {
        scoreType = Cosine.SCORE_LTN;
        this.computeScore();
    }

    public void computeBM25Score(Double importanceSaturationTf, Double importanceNormalisationTaille) {
        scoreType = Cosine.SCORE_BM25;
        this.importanceSaturationTf = importanceSaturationTf;
        this.importanceNormalisationTaille = importanceNormalisationTaille;
        this.computeScore();
    }

    /**
     * Calcule le score des documents pour les differentes requetes
     */
    private void computeScore() {
        String[] documentIds;
        this.scores = new LinkedHashMap<String, QueryScore>();

        documentIds = documentCollectionIndex.getDocumentsIds();

        for (String docId : documentIds) {
            this.documentCollectionIndex.getIndexedDocument(docId).reinitializeScores();

            for (DocumentIndex query : queries)
                computeDocumentScoreForQuery(docId, query);
        }

        sortQueriesScores();
    }

    private void computeDocumentScoreForQuery(String docId, DocumentIndex query) {
        String queryId;
        String[] terms;

        queryId = query.getDocumentId();
        terms = query.getAllTerms();

        tmpScore = (scores.containsKey(queryId)) ?
                scores.get(queryId) :
                new QueryScore();

        for (String term : terms)
            computeDocumentScoreForTerm(docId, term);

        scores.put(queryId, tmpScore);
    }

    private void computeDocumentScoreForTerm(String docId, String term) {
        if (documentCollectionIndex.getIndexedDocument(docId).containsTerm(term)) { // si le document contient le terme
            if (!tmpScore.containsKey(docId))
                // si le score du document n'a pas commence d'etre calcule, on initialise
                tmpScore.put(docId, 0.0);

            if (scoreType == Cosine.SCORE_LTN)
                // ajout du score ltn du document docId pour le terme term
                tmpScore.put(docId, tmpScore.get(docId) + documentCollectionIndex.computeLtn(docId, term));

            else if (scoreType == Cosine.SCORE_BM25)
                // ajout du score bm25 du document docId pour le terme term
                tmpScore.put(docId, tmpScore.get(docId) + documentCollectionIndex.computeBm25(docId, term,
                        importanceSaturationTf,
                        importanceNormalisationTaille));
        }
    }

    private void sortQueriesScores() {
        for (QueryScore queryScore : scores.values())
            queryScore.sortByScore();
    }

    /**
     * Ecrit dans un fichier le resultat de compute()
     */
    public void write(String runNumber, String type) {
        String[] queryIds = this.scores.keySet().toArray(new String[this.scores.size()]);
        String fileName;
        String team = "AlexandreFabienJeremySofiia";
        QueryScore score;
        int i;
        PrintWriter out;

        if (scoreType == Cosine.SCORE_LTN)
            fileName = team + "_" + runNumber + "_ltn_"+type+".txt";
        else
            fileName = team + "_" + runNumber + "_bm25_"+type+"_k" + importanceSaturationTf + "b" +
                    importanceNormalisationTaille + ".txt";

        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
            System.out.println("Writing in file '" + fileName + "'...\n");

            for (String queryId : queryIds) {
                score = scores.get(queryId);

                i = 1;
                for (Map.Entry<String, Double> entree : score.entrySet()) {
                    out.println(queryId + " Q0 " + entree.getKey() + " " + i++ + " "
                            + this.formatScore(entree.getValue()) + " " + team + " /article[1]");

                    if (i > 1500)
                        break;
                }
            }

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatScore(Double score) {
        DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
        otherSymbols.setDecimalSeparator('.');
        DecimalFormat df = new DecimalFormat("#.##########", otherSymbols);

        return df.format(score);
    }

    public LinkedHashMap<String, QueryScore> getScores() {
        return scores;
    }
}

