package org.ucair.text;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.base.Preconditions;

public class TermVector {

    private final Map<String, Double> values = new HashMap<String, Double>();

    private Double length;

    private boolean immutable = false;

    public TermVector() {
    }

    public TermVector(final Map<String, Double> values) {
        this.values.putAll(values);
    }

    public TermVector(final Collection<String> terms) {
        for (final String term : terms) {
            final Double value = values.get(term);
            values.put(term, value == null ? 1 : value + 1);
        }
    }

    public Set<String> getTerms() {
        return values.keySet();
    }

    public Map<String, Double> getValues() {
        return Collections.unmodifiableMap(values);
    }

    public void put(final String term, final double value) {
        Preconditions
                .checkArgument(!immutable, "This term vector is immutable");
        length = null;
        values.put(term, value);
    }

    public double get(final String term) {
        final Double value = values.get(term);
        return value == null ? 0.0 : value;
    }

    public double getLength() {
        if (length == null) {
            length = 0.0;
            for (final double value : values.values()) {
                length += value;
            }
        }
        return length;
    }

    public double getL2Norm() {
        double l2Norm = 0.0;
        for (final double value : values.values()) {
            l2Norm += value * value;
        }
        l2Norm = Math.sqrt(l2Norm);
        return l2Norm;
    }

    public void setImmutable() {
        immutable = true;
    }

    public TermVector truncate(final int maxTerms, final double minValue) {
        final List<Map.Entry<String, Double>> terms = new ArrayList<Map.Entry<String, Double>>(
                values.entrySet());
        Collections.sort(terms, new Comparator<Map.Entry<String, Double>>() {

            @Override
            public int compare(final Entry<String, Double> o1,
                    final Entry<String, Double> o2) {
                return -o1.getValue().compareTo(o2.getValue());
            }
        });

        int i = 0;
        for (; i < terms.size() && i < maxTerms; ++i) {
            if (terms.get(i).getValue() < minValue) {
                break;
            }
        }
        terms.subList(i, terms.size()).clear();

        final Map<String, Double> truncated = new HashMap<String, Double>();
        for (final Map.Entry<String, Double> entry : terms) {
            truncated.put(entry.getKey(), entry.getValue());
        }

        return new TermVector(truncated);
    }

    @Override
    public String toString() {
        final StringBuilder buffer = new StringBuilder();
        for (final Map.Entry<String, Double> entry : values.entrySet()) {
            if (buffer.length() > 0) {
                buffer.append(", ");
            }
            buffer.append(entry.getKey() + "=" + entry.getValue());
        }
        return new ToStringBuilder(this).append("values", buffer.toString())
                .toString();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final TermVector other = (TermVector) obj;
        return new EqualsBuilder().append(values, other.values).isEquals();
    }
}

