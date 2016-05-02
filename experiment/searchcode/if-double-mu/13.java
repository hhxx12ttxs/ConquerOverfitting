package org.ucair.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

@Component("dirPriorSearcher")
public class TermVectorSearcherDirichletPriorImpl implements TermVectorSearcher {

    @Inject
    private CollectionModel collectionModel;

    private final double mu = 20.0;

    public void setCollectionModel(final CollectionModel collectionModel) {
        this.collectionModel = collectionModel;
    }

    private void sortAndTruncate(final List<Pair<Integer, Double>> docScores,
            final int topK) {
        Collections.sort(docScores, new Comparator<Pair<Integer, Double>>() {

            @Override
            public int compare(final Pair<Integer, Double> a,
                    final Pair<Integer, Double> b) {
                return -Double.compare(a.getRight(), b.getRight());
            }
        });

        if (topK > 0 && docScores.size() > topK) {
            docScores.subList(topK, docScores.size()).clear();
        }
    }

    @Override
    public List<Pair<Integer, Double>> search(final TermVectorIndex index,
            final TermVector query, final int topK) {
        final Map<Integer, Double> docIdToScore = new HashMap<Integer, Double>();

        double bgLikelihood = 0.0;

        for (final Map.Entry<String, Double> termEntry : query.getValues()
                .entrySet()) {
            final String term = termEntry.getKey();
            final double queryTermWeight = termEntry.getValue();

            final double bgTermProb = collectionModel.getTermProb(term);
            bgLikelihood += queryTermWeight * Math.log(bgTermProb);

            for (final Map.Entry<Integer, Double> docEntry : index.getPostings(
                    term).entrySet()) {
                final int docId = docEntry.getKey();
                final double docTermWeight = docEntry.getValue();
                final double scoreDelta = queryTermWeight
                        * Math.log(1.0 + docTermWeight / (mu * bgTermProb));
                TextUtil.increment(docIdToScore, docId, scoreDelta);
            }
        }

        final List<Pair<Integer, Double>> docScores = new ArrayList<Pair<Integer, Double>>(
                docIdToScore.size());
        for (final Map.Entry<Integer, Double> scoreEntry : docIdToScore
                .entrySet()) {
            final int docId = scoreEntry.getKey();
            double score = scoreEntry.getValue();
            final TermVector doc = index.getDoc(docId);
            score = (score + bgLikelihood) / query.getLength()
                    + Math.log(mu / (doc.getLength() + mu));
            docScores.add(Pair.of(docId, score));
        }

        sortAndTruncate(docScores, topK);

        return docScores;
    }
}

