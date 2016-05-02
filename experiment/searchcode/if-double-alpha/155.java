package org.ucair.text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

public class MixtureEstimator {

    public static TermVector estimateMixtureComponent(final TermVector target,
            final CollectionModel background, final double bgWeight) {
        final TermVector bg = new TermVector();
        for (final String term : target.getTerms()) {
            bg.put(term, background.getTermProb(term));
        }
        final TermVector result = estimateMixtureComponent(target, bg, bgWeight);
        return result;
    }

    /**
     * Computes the maximum likelihood estimate of an unknown component in a
     * two-component mixture language model.
     * 
     * We want to maximize \sum f_i log(\alpha * p_i + (1 - \alpha * q_i) f, p,
     * alpha is known, and q is to be estimated. See the paper by Yi Zhang, Wei
     * Xu: Fast Exact Maximum Likelihood Estimation for Mixture of Language
     * Models
     */
    private static TermVector estimateMixtureComponent(final TermVector tv_f,
            final TermVector tv_p, final double alpha) {

        class Record {
            String term;
            double f;
            double p;
            double q;
            Double p_over_f;
        }

        Preconditions.checkArgument(alpha > 0.0 && alpha < 1.0,
                "Invalid alpha: %s", alpha);
        final double beta = 1.0 - alpha;

        final List<Record> records = new ArrayList<Record>();

        for (final Map.Entry<String, Double> entry : tv_f.getValues()
                .entrySet()) {
            final String term = entry.getKey();
            final double f = entry.getValue();
            Double p = tv_p.get(term);
            if (p == null) {
                p = 0.0;
            }

            final Record record = new Record();
            record.term = term;
            record.f = f;
            record.p = p;
            record.p_over_f = p / f;
            records.add(record);
        }

        Collections.sort(records, new Comparator<Record>() {
            @Override
            public int compare(final Record a, final Record b) {
                return a.p_over_f.compareTo(b.p_over_f);
            }
        });

        double f_sum = 0.0, p_sum = 0.0;
        int k = 0;
        for (final Record record : records) {
            f_sum += record.f;
            p_sum += record.p;
            if (beta / alpha + p_sum <= f_sum * record.p_over_f) {
                f_sum -= record.f;
                p_sum -= record.p;
                break;
            }
            ++k;
        }

        final double lambda = f_sum / (1.0 + alpha / beta * p_sum);
        int i = 0;
        for (final Record record : records) {
            if (i == k) {
                break;
            }
            record.q = record.f / lambda - alpha / beta * record.p;
            ++i;
        }

        final TermVector tv_q = new TermVector();
        for (final Record record : records) {
            if (record.q > 0.0) {
                tv_q.put(record.term, record.q);
            }
        }
        return tv_q;
    }
}

