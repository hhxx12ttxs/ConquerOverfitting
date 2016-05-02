/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

<<<<<<< HEAD
package org.elasticsearch.search.facet.histogram.unbounded;

import org.elasticsearch.common.CacheRecycler;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.trove.ExtTLongObjectHashMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.histogram.HistogramFacet;
import org.elasticsearch.search.facet.histogram.InternalHistogramFacet;

import java.io.IOException;
import java.util.*;

/**
 * @author kimchy (shay.banon)
 */
public class InternalFullHistogramFacet extends InternalHistogramFacet {

    private static final String STREAM_TYPE = "fHistogram";

    public static void registerStreams() {
=======
package org.elasticsearch.search.facet.termsstats.doubles;

import org.elasticsearch.common.CacheRecycler;
import org.elasticsearch.common.collect.ImmutableList;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.trove.ExtTDoubleObjectHashMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.termsstats.InternalTermsStatsFacet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class InternalTermsStatsDoubleFacet extends InternalTermsStatsFacet {

    private static final String STREAM_TYPE = "dTS";

    public static void registerStream() {
>>>>>>> 76aa07461566a5976980e6696204781271955163
        Streams.registerStream(STREAM, STREAM_TYPE);
    }

    static Stream STREAM = new Stream() {
        @Override public Facet readFacet(String type, StreamInput in) throws IOException {
<<<<<<< HEAD
            return readHistogramFacet(in);
=======
            return readTermsStatsFacet(in);
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    };

    @Override public String streamType() {
        return STREAM_TYPE;
    }

<<<<<<< HEAD

    /**
     * A histogram entry representing a single entry within the result of a histogram facet.
     */
    public static class FullEntry implements Entry {
        long key;
        long count;
        long totalCount;
        double total;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        public FullEntry(long key, long count, double min, double max, long totalCount, double total) {
            this.key = key;
            this.count = count;
            this.min = min;
            this.max = max;
            this.totalCount = totalCount;
            this.total = total;
        }

        @Override public long key() {
            return key;
        }

        @Override public long getKey() {
            return key();
        }

        @Override public long count() {
            return count;
        }

        @Override public long getCount() {
            return count();
        }

        @Override public double total() {
            return total;
        }

        @Override public double getTotal() {
            return total();
        }

        @Override public long totalCount() {
            return totalCount;
=======
    public InternalTermsStatsDoubleFacet() {
    }

    public static class DoubleEntry implements Entry {

        double term;
        long count;
        long totalCount;
        double total;
        double min;
        double max;

        public DoubleEntry(double term, long count, long totalCount, double total, double min, double max) {
            this.term = term;
            this.count = count;
            this.total = total;
            this.totalCount = totalCount;
            this.min = min;
            this.max = max;
        }

        @Override public String term() {
            return Double.toString(term);
        }

        @Override public String getTerm() {
            return term();
        }

        @Override public Number termAsNumber() {
            return term;
        }

        @Override public Number getTermAsNumber() {
            return termAsNumber();
        }

        @Override public long count() {
            return count;
        }

        @Override public long getCount() {
            return count();
        }

        @Override public long totalCount() {
            return this.totalCount;
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }

        @Override public long getTotalCount() {
            return this.totalCount;
        }

<<<<<<< HEAD
        @Override public double mean() {
            return total / totalCount;
        }

        @Override public double getMean() {
            return total / totalCount;
        }

=======
>>>>>>> 76aa07461566a5976980e6696204781271955163
        @Override public double min() {
            return this.min;
        }

        @Override public double getMin() {
<<<<<<< HEAD
            return this.min;
        }

        @Override public double max() {
            return this.max;
        }

        @Override public double getMax() {
            return this.max;
=======
            return min();
        }

        @Override public double max() {
            return max;
        }

        @Override public double getMax() {
            return max();
        }

        @Override public double total() {
            return total;
        }

        @Override public double getTotal() {
            return total();
        }

        @Override public double mean() {
            if (totalCount == 0) {
                return 0;
            }
            return total / totalCount;
        }

        @Override public double getMean() {
            return mean();
        }

        @Override public int compareTo(Entry o) {
            DoubleEntry other = (DoubleEntry) o;
            return (term < other.term ? -1 : (term == other.term ? 0 : 1));
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    private String name;

<<<<<<< HEAD
    private ComparatorType comparatorType;

    ExtTLongObjectHashMap<InternalFullHistogramFacet.FullEntry> tEntries;
    boolean cachedEntries;
    Collection<FullEntry> entries;

    private InternalFullHistogramFacet() {
    }

    public InternalFullHistogramFacet(String name, ComparatorType comparatorType, ExtTLongObjectHashMap<InternalFullHistogramFacet.FullEntry> entries, boolean cachedEntries) {
        this.name = name;
        this.comparatorType = comparatorType;
        this.tEntries = entries;
        this.cachedEntries = cachedEntries;
        this.entries = entries.valueCollection();
=======
    int requiredSize;

    long missing;

    Collection<DoubleEntry> entries = ImmutableList.of();

    ComparatorType comparatorType;

    public InternalTermsStatsDoubleFacet(String name, ComparatorType comparatorType, int requiredSize, Collection<DoubleEntry> entries, long missing) {
        this.name = name;
        this.comparatorType = comparatorType;
        this.requiredSize = requiredSize;
        this.entries = entries;
        this.missing = missing;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override public String name() {
        return this.name;
    }

    @Override public String getName() {
<<<<<<< HEAD
        return name();
=======
        return this.name;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override public String type() {
        return TYPE;
    }

    @Override public String getType() {
        return type();
    }

<<<<<<< HEAD
    @Override public List<FullEntry> entries() {
        if (!(entries instanceof List)) {
            entries = new ArrayList<FullEntry>(entries);
        }
        return (List<FullEntry>) entries;
    }

    @Override public List<FullEntry> getEntries() {
        return entries();
    }

    @Override public Iterator<Entry> iterator() {
        return (Iterator) entries().iterator();
    }

    void releaseCache() {
        if (cachedEntries) {
            CacheRecycler.pushLongObjectMap(tEntries);
            cachedEntries = false;
            tEntries = null;
        }
=======
    @Override public List<DoubleEntry> entries() {
        if (!(entries instanceof List)) {
            entries = ImmutableList.copyOf(entries);
        }
        return (List<DoubleEntry>) entries;
    }

    List<DoubleEntry> mutableList() {
        if (!(entries instanceof List)) {
            entries = new ArrayList<DoubleEntry>(entries);
        }
        return (List<DoubleEntry>) entries;
    }

    @Override public List<DoubleEntry> getEntries() {
        return entries();
    }

    @SuppressWarnings({"unchecked"}) @Override public Iterator<Entry> iterator() {
        return (Iterator) entries.iterator();
    }

    @Override public long missingCount() {
        return this.missing;
    }

    @Override public long getMissingCount() {
        return missingCount();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override public Facet reduce(String name, List<Facet> facets) {
        if (facets.size() == 1) {
<<<<<<< HEAD
            // we need to sort it
            InternalFullHistogramFacet internalFacet = (InternalFullHistogramFacet) facets.get(0);
            List<FullEntry> entries = internalFacet.entries();
            Collections.sort(entries, comparatorType.comparator());
            internalFacet.releaseCache();
            return internalFacet;
        }

        ExtTLongObjectHashMap<FullEntry> map = CacheRecycler.popLongObjectMap();

        for (Facet facet : facets) {
            InternalFullHistogramFacet histoFacet = (InternalFullHistogramFacet) facet;
            for (FullEntry fullEntry : histoFacet.entries) {
                FullEntry current = map.get(fullEntry.key);
                if (current != null) {
                    current.count += fullEntry.count;
                    current.total += fullEntry.total;
                    current.totalCount += fullEntry.totalCount;
                    if (fullEntry.min < current.min) {
                        current.min = fullEntry.min;
                    }
                    if (fullEntry.max > current.max) {
                        current.max = fullEntry.max;
                    }
                } else {
                    map.put(fullEntry.key, fullEntry);
                }
            }
            histoFacet.releaseCache();
        }

        // sort
        Object[] values = map.internalValues();
        Arrays.sort(values, (Comparator) comparatorType.comparator());
        List<FullEntry> ordered = new ArrayList<FullEntry>(map.size());
        for (int i = 0; i < map.size(); i++) {
            FullEntry value = (FullEntry) values[i];
            if (value == null) {
                break;
            }
            ordered.add(value);
        }

        CacheRecycler.pushLongObjectMap(map);

        // just initialize it as already ordered facet
        InternalFullHistogramFacet ret = new InternalFullHistogramFacet();
        ret.name = name;
        ret.comparatorType = comparatorType;
        ret.entries = ordered;
        return ret;
=======
            if (requiredSize == 0) {
                // we need to sort it here!
                InternalTermsStatsDoubleFacet tsFacet = (InternalTermsStatsDoubleFacet) facets.get(0);
                if (!tsFacet.entries.isEmpty()) {
                    List<DoubleEntry> entries = tsFacet.mutableList();
                    Collections.sort(entries, comparatorType.comparator());
                }
            }
            return facets.get(0);
        }
        int missing = 0;
        ExtTDoubleObjectHashMap<DoubleEntry> map = CacheRecycler.popDoubleObjectMap();
        map.clear();
        for (Facet facet : facets) {
            InternalTermsStatsDoubleFacet tsFacet = (InternalTermsStatsDoubleFacet) facet;
            missing += tsFacet.missing;
            for (Entry entry : tsFacet) {
                DoubleEntry doubleEntry = (DoubleEntry) entry;
                DoubleEntry current = map.get(doubleEntry.term);
                if (current != null) {
                    current.count += doubleEntry.count;
                    current.totalCount += doubleEntry.totalCount;
                    current.total += doubleEntry.total;
                    if (doubleEntry.min < current.min) {
                        current.min = doubleEntry.min;
                    }
                    if (doubleEntry.max > current.max) {
                        current.max = doubleEntry.max;
                    }
                } else {
                    map.put(doubleEntry.term, doubleEntry);
                }
            }
        }

        // sort
        if (requiredSize == 0) { // all terms
            DoubleEntry[] entries1 = map.values(new DoubleEntry[map.size()]);
            Arrays.sort(entries1, comparatorType.comparator());
            CacheRecycler.pushDoubleObjectMap(map);
            return new InternalTermsStatsDoubleFacet(name, comparatorType, requiredSize, Arrays.asList(entries1), missing);
        } else {
            Object[] values = map.internalValues();
            Arrays.sort(values, (Comparator) comparatorType.comparator());
            List<DoubleEntry> ordered = new ArrayList<DoubleEntry>(map.size());
            for (int i = 0; i < requiredSize; i++) {
                DoubleEntry value = (DoubleEntry) values[i];
                if (value == null) {
                    break;
                }
                ordered.add(value);
            }
            CacheRecycler.pushDoubleObjectMap(map);
            return new InternalTermsStatsDoubleFacet(name, comparatorType, requiredSize, ordered, missing);
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    static final class Fields {
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
<<<<<<< HEAD
        static final XContentBuilderString ENTRIES = new XContentBuilderString("entries");
        static final XContentBuilderString KEY = new XContentBuilderString("key");
        static final XContentBuilderString COUNT = new XContentBuilderString("count");
        static final XContentBuilderString TOTAL = new XContentBuilderString("total");
        static final XContentBuilderString TOTAL_COUNT = new XContentBuilderString("total_count");
        static final XContentBuilderString MEAN = new XContentBuilderString("mean");
        static final XContentBuilderString MIN = new XContentBuilderString("min");
        static final XContentBuilderString MAX = new XContentBuilderString("max");
=======
        static final XContentBuilderString MISSING = new XContentBuilderString("missing");
        static final XContentBuilderString TERMS = new XContentBuilderString("terms");
        static final XContentBuilderString TERM = new XContentBuilderString("term");
        static final XContentBuilderString COUNT = new XContentBuilderString("count");
        static final XContentBuilderString TOTAL_COUNT = new XContentBuilderString("total_count");
        static final XContentBuilderString MIN = new XContentBuilderString("min");
        static final XContentBuilderString MAX = new XContentBuilderString("max");
        static final XContentBuilderString TOTAL = new XContentBuilderString("total");
        static final XContentBuilderString MEAN = new XContentBuilderString("mean");
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
<<<<<<< HEAD
        builder.field(Fields._TYPE, HistogramFacet.TYPE);
        builder.startArray(Fields.ENTRIES);
        for (Entry entry : entries) {
            builder.startObject();
            builder.field(Fields.KEY, entry.key());
            builder.field(Fields.COUNT, entry.count());
            builder.field(Fields.MIN, entry.min());
            builder.field(Fields.MAX, entry.max());
            builder.field(Fields.TOTAL, entry.total());
            builder.field(Fields.TOTAL_COUNT, entry.totalCount());
=======
        builder.field(Fields._TYPE, InternalTermsStatsFacet.TYPE);
        builder.field(Fields.MISSING, missing);
        builder.startArray(Fields.TERMS);
        for (Entry entry : entries) {
            builder.startObject();
            builder.field(Fields.TERM, ((DoubleEntry) entry).term);
            builder.field(Fields.COUNT, entry.count());
            builder.field(Fields.TOTAL_COUNT, entry.totalCount());
            builder.field(Fields.MIN, entry.min());
            builder.field(Fields.MAX, entry.max());
            builder.field(Fields.TOTAL, entry.total());
>>>>>>> 76aa07461566a5976980e6696204781271955163
            builder.field(Fields.MEAN, entry.mean());
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

<<<<<<< HEAD
    public static InternalFullHistogramFacet readHistogramFacet(StreamInput in) throws IOException {
        InternalFullHistogramFacet facet = new InternalFullHistogramFacet();
=======
    public static InternalTermsStatsDoubleFacet readTermsStatsFacet(StreamInput in) throws IOException {
        InternalTermsStatsDoubleFacet facet = new InternalTermsStatsDoubleFacet();
>>>>>>> 76aa07461566a5976980e6696204781271955163
        facet.readFrom(in);
        return facet;
    }

    @Override public void readFrom(StreamInput in) throws IOException {
        name = in.readUTF();
        comparatorType = ComparatorType.fromId(in.readByte());
<<<<<<< HEAD

        cachedEntries = false;
        int size = in.readVInt();
        entries = new ArrayList<FullEntry>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new FullEntry(in.readLong(), in.readVLong(), in.readDouble(), in.readDouble(), in.readVLong(), in.readDouble()));
=======
        requiredSize = in.readVInt();
        missing = in.readVLong();

        int size = in.readVInt();
        entries = new ArrayList<DoubleEntry>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new DoubleEntry(in.readDouble(), in.readVLong(), in.readVLong(), in.readDouble(), in.readDouble(), in.readDouble()));
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    @Override public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(name);
        out.writeByte(comparatorType.id());
<<<<<<< HEAD
        out.writeVInt(entries.size());
        for (FullEntry entry : entries) {
            out.writeLong(entry.key);
            out.writeVLong(entry.count);
            out.writeDouble(entry.min);
            out.writeDouble(entry.max);
            out.writeVLong(entry.totalCount);
            out.writeDouble(entry.total);
        }
        releaseCache();
=======
        out.writeVInt(requiredSize);
        out.writeVLong(missing);

        out.writeVInt(entries.size());
        for (Entry entry : entries) {
            out.writeDouble(((DoubleEntry) entry).term);
            out.writeVLong(entry.count());
            out.writeVLong(entry.totalCount());
            out.writeDouble(entry.total());
            out.writeDouble(entry.min());
            out.writeDouble(entry.max());
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}
