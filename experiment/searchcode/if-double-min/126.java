/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
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
package org.elasticsearch.search.facet.histogram.bounded;

import org.elasticsearch.common.CacheRecycler;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.histogram.HistogramFacet;
import org.elasticsearch.search.facet.histogram.InternalHistogramFacet;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class InternalBoundedFullHistogramFacet extends InternalHistogramFacet {

    private static final String STREAM_TYPE = "fBdHistogram";

    public static void registerStreams() {
=======
package org.elasticsearch.search.facet.termsstats.strings;

import com.google.common.collect.ImmutableList;
import org.elasticsearch.common.CacheRecycler;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.trove.ExtTHashMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;
import org.elasticsearch.search.facet.termsstats.InternalTermsStatsFacet;

import java.io.IOException;
import java.util.*;

public class InternalTermsStatsStringFacet extends InternalTermsStatsFacet {

    private static final String STREAM_TYPE = "tTS";

    public static void registerStream() {
>>>>>>> 76aa07461566a5976980e6696204781271955163
        Streams.registerStream(STREAM, STREAM_TYPE);
    }

    static Stream STREAM = new Stream() {
        @Override
        public Facet readFacet(String type, StreamInput in) throws IOException {
<<<<<<< HEAD
            return readHistogramFacet(in);
=======
            return readTermsStatsFacet(in);
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    };

    @Override
    public String streamType() {
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

        @Override
        public long key() {
            return key;
        }

        @Override
        public long getKey() {
            return key();
=======
    public InternalTermsStatsStringFacet() {
    }

    public static class StringEntry implements Entry {

        String term;
        long count;
        long totalCount;
        double total;
        double min;
        double max;

        public StringEntry(String term, long count, long totalCount, double total, double min, double max) {
            this.term = term;
            this.count = count;
            this.totalCount = totalCount;
            this.total = total;
            this.min = min;
            this.max = max;
        }

        @Override
        public String term() {
            return term;
        }

        @Override
        public String getTerm() {
            return term();
        }

        @Override
        public Number termAsNumber() {
            return Double.parseDouble(term);
        }

        @Override
        public Number getTermAsNumber() {
            return termAsNumber();
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }

        @Override
        public long count() {
            return count;
        }

        @Override
        public long getCount() {
            return count();
        }

        @Override
<<<<<<< HEAD
        public double total() {
            return total;
        }

        @Override
        public double getTotal() {
            return total();
        }

        @Override
        public long totalCount() {
            return totalCount;
        }

        @Override
        public long getTotalCount() {
            return this.totalCount;
        }

        @Override
        public double mean() {
            if (totalCount == 0) {
                return 0;
            }
            return total / totalCount;
        }

        @Override
        public double getMean() {
            return total / totalCount;
        }

        @Override
        public double min() {
            return this.min;
        }

        @Override
        public double getMin() {
            return this.min;
        }

        @Override
        public double max() {
            return this.max;
        }

        @Override
        public double getMax() {
            return this.max;
=======
        public long totalCount() {
            return this.totalCount;
        }

        @Override
        public long getTotalCount() {
            return this.totalCount;
        }

        @Override
        public double min() {
            return this.min;
        }

        @Override
        public double getMin() {
            return min();
        }

        @Override
        public double max() {
            return this.max;
        }

        @Override
        public double getMax() {
            return max();
        }

        @Override
        public double total() {
            return total;
        }

        @Override
        public double getTotal() {
            return total();
        }

        @Override
        public double mean() {
            if (totalCount == 0) {
                return 0;
            }
            return total / totalCount;
        }

        @Override
        public double getMean() {
            return mean();
        }

        @Override
        public int compareTo(Entry o) {
            return term.compareTo(o.term());
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    private String name;

<<<<<<< HEAD
    private ComparatorType comparatorType;

    Object[] entries;
    List<Object> entriesList;
    boolean cachedEntries;
    int size;
    long interval;
    long offset;
    boolean normalized;

    private InternalBoundedFullHistogramFacet() {
    }

    public InternalBoundedFullHistogramFacet(String name, ComparatorType comparatorType, long interval, long offset, int size, Object[] entries, boolean cachedEntries) {
        this.name = name;
        this.comparatorType = comparatorType;
        this.interval = interval;
        this.offset = offset;
        this.size = size;
        this.entries = entries;
        this.cachedEntries = cachedEntries;
=======
    int requiredSize;

    long missing;

    Collection<StringEntry> entries = ImmutableList.of();

    ComparatorType comparatorType;

    public InternalTermsStatsStringFacet(String name, ComparatorType comparatorType, int requiredSize, Collection<StringEntry> entries, long missing) {
        this.name = name;
        this.comparatorType = comparatorType;
        this.requiredSize = requiredSize;
        this.entries = entries;
        this.missing = missing;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override
    public String name() {
        return this.name;
    }

    @Override
    public String getName() {
<<<<<<< HEAD
        return name();
=======
        return this.name;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public String getType() {
        return type();
    }

    @Override
<<<<<<< HEAD
    public List<FullEntry> entries() {
        normalize();
        if (entriesList == null) {
            Object[] newEntries = new Object[size];
            System.arraycopy(entries, 0, newEntries, 0, size);
            entriesList = Arrays.asList(newEntries);
        }
        releaseCache();
        return (List) entriesList;
    }

    @Override
    public List<FullEntry> getEntries() {
        return entries();
    }

    @Override
    public Iterator<Entry> iterator() {
        return (Iterator) entries().iterator();
    }

    private void releaseCache() {
        if (cachedEntries) {
            cachedEntries = false;
            CacheRecycler.pushObjectArray(entries);
        }
=======
    public List<StringEntry> entries() {
        if (!(entries instanceof List)) {
            entries = ImmutableList.copyOf(entries);
        }
        return (List<StringEntry>) entries;
    }

    List<StringEntry> mutableList() {
        if (!(entries instanceof List)) {
            entries = new ArrayList<StringEntry>(entries);
        }
        return (List<StringEntry>) entries;
    }

    @Override
    public List<StringEntry> getEntries() {
        return entries();
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public Iterator<Entry> iterator() {
        return (Iterator) entries.iterator();
    }

    @Override
    public long missingCount() {
        return this.missing;
    }

    @Override
    public long getMissingCount() {
        return missingCount();
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override
    public Facet reduce(String name, List<Facet> facets) {
        if (facets.size() == 1) {
<<<<<<< HEAD
            // we need to sort it
            InternalBoundedFullHistogramFacet internalFacet = (InternalBoundedFullHistogramFacet) facets.get(0);
            if (comparatorType != ComparatorType.KEY) {
                Arrays.sort(internalFacet.entries, (Comparator) comparatorType.comparator());
            }
            return internalFacet;
        }

        InternalBoundedFullHistogramFacet first = (InternalBoundedFullHistogramFacet) facets.get(0);

        for (int f = 1; f < facets.size(); f++) {
            InternalBoundedFullHistogramFacet internalFacet = (InternalBoundedFullHistogramFacet) facets.get(f);
            for (int i = 0; i < size; i++) {
                FullEntry aggEntry = (FullEntry) first.entries[i];
                FullEntry entry = (FullEntry) internalFacet.entries[i];
                if (aggEntry == null) {
                    first.entries[i] = entry;
                } else if (entry != null) {
                    aggEntry.count += entry.count;
                    aggEntry.totalCount += entry.totalCount;
                    aggEntry.total += entry.total;
                    if (entry.min < aggEntry.min) {
                        aggEntry.min = entry.min;
                    }
                    if (entry.max > aggEntry.max) {
                        aggEntry.max = entry.max;
                    }
                }
            }
            internalFacet.releaseCache();
        }

        if (comparatorType != ComparatorType.KEY) {
            Arrays.sort(first.entries, (Comparator) comparatorType.comparator());
        }

        return first;
    }

    private void normalize() {
        if (normalized) {
            return;
        }
        normalized = true;
        for (int i = 0; i < size; i++) {
            FullEntry entry = (FullEntry) entries[i];
            if (entry == null) {
                entries[i] = new FullEntry((i * interval) + offset, 0, Double.NaN, Double.NaN, 0, 0);
            } else {
                entry.key = (i * interval) + offset;
            }
=======
            if (requiredSize == 0) {
                // we need to sort it here!
                InternalTermsStatsStringFacet tsFacet = (InternalTermsStatsStringFacet) facets.get(0);
                if (!tsFacet.entries.isEmpty()) {
                    List<StringEntry> entries = tsFacet.mutableList();
                    Collections.sort(entries, comparatorType.comparator());
                }
            }
            return facets.get(0);
        }
        int missing = 0;
        ExtTHashMap<String, StringEntry> map = CacheRecycler.popHashMap();
        for (Facet facet : facets) {
            InternalTermsStatsStringFacet tsFacet = (InternalTermsStatsStringFacet) facet;
            missing += tsFacet.missing;
            for (Entry entry : tsFacet) {
                StringEntry stringEntry = (StringEntry) entry;
                StringEntry current = map.get(stringEntry.term());
                if (current != null) {
                    current.count += stringEntry.count;
                    current.totalCount += stringEntry.totalCount;
                    current.total += stringEntry.total;
                    if (stringEntry.min < current.min) {
                        current.min = stringEntry.min;
                    }
                    if (stringEntry.max > current.max) {
                        current.max = stringEntry.max;
                    }
                } else {
                    map.put(stringEntry.term(), stringEntry);
                }
            }
        }

        // sort
        if (requiredSize == 0) { // all terms
            StringEntry[] entries1 = map.values().toArray(new StringEntry[map.size()]);
            Arrays.sort(entries1, comparatorType.comparator());
            CacheRecycler.pushHashMap(map);
            return new InternalTermsStatsStringFacet(name, comparatorType, requiredSize, Arrays.asList(entries1), missing);
        } else {
            Object[] values = map.internalValues();
            Arrays.sort(values, (Comparator) comparatorType.comparator());
            List<StringEntry> ordered = new ArrayList<StringEntry>(map.size());
            for (int i = 0; i < requiredSize; i++) {
                StringEntry value = (StringEntry) values[i];
                if (value == null) {
                    break;
                }
                ordered.add(value);
            }
            CacheRecycler.pushHashMap(map);
            return new InternalTermsStatsStringFacet(name, comparatorType, requiredSize, ordered, missing);
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
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
        static final XContentBuilderString TOTAL = new XContentBuilderString("total");
        static final XContentBuilderString MIN = new XContentBuilderString("min");
        static final XContentBuilderString MAX = new XContentBuilderString("max");
        static final XContentBuilderString MEAN = new XContentBuilderString("mean");
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
<<<<<<< HEAD
        builder.field(Fields._TYPE, HistogramFacet.TYPE);
        builder.startArray(Fields.ENTRIES);
        for (int i = 0; i < size; i++) {
            FullEntry entry = (FullEntry) entries[i];
            builder.startObject();
            if (normalized) {
                builder.field(Fields.KEY, entry.key());
            } else {
                builder.field(Fields.KEY, (i * interval) + offset);
            }
            if (entry == null) {
                builder.field(Fields.COUNT, 0);
                builder.field(Fields.TOTAL, 0);
                builder.field(Fields.TOTAL_COUNT, 0);
            } else {
                builder.field(Fields.COUNT, entry.count());
                builder.field(Fields.MIN, entry.min());
                builder.field(Fields.MAX, entry.max());
                builder.field(Fields.TOTAL, entry.total());
                builder.field(Fields.TOTAL_COUNT, entry.totalCount());
                builder.field(Fields.MEAN, entry.mean());
            }
=======
        builder.field(Fields._TYPE, InternalTermsStatsFacet.TYPE);
        builder.field(Fields.MISSING, missing);
        builder.startArray(Fields.TERMS);
        for (Entry entry : entries) {
            builder.startObject();
            builder.field(Fields.TERM, entry.term());
            builder.field(Fields.COUNT, entry.count());
            builder.field(Fields.TOTAL_COUNT, entry.totalCount());
            builder.field(Fields.MIN, entry.min());
            builder.field(Fields.MAX, entry.max());
            builder.field(Fields.TOTAL, entry.total());
            builder.field(Fields.MEAN, entry.mean());
>>>>>>> 76aa07461566a5976980e6696204781271955163
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
<<<<<<< HEAD
        releaseCache();
        return builder;
    }

    public static InternalBoundedFullHistogramFacet readHistogramFacet(StreamInput in) throws IOException {
        InternalBoundedFullHistogramFacet facet = new InternalBoundedFullHistogramFacet();
=======
        return builder;
    }

    public static InternalTermsStatsStringFacet readTermsStatsFacet(StreamInput in) throws IOException {
        InternalTermsStatsStringFacet facet = new InternalTermsStatsStringFacet();
>>>>>>> 76aa07461566a5976980e6696204781271955163
        facet.readFrom(in);
        return facet;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        name = in.readUTF();
        comparatorType = ComparatorType.fromId(in.readByte());
<<<<<<< HEAD

        offset = in.readLong();
        interval = in.readVLong();
        size = in.readVInt();
        entries = CacheRecycler.popObjectArray(size);
        cachedEntries = true;
        for (int i = 0; i < size; i++) {
            if (in.readBoolean()) {
                entries[i] = new FullEntry(i, in.readVLong(), in.readDouble(), in.readDouble(), in.readVLong(), in.readDouble());
            }
=======
        requiredSize = in.readVInt();
        missing = in.readVLong();

        int size = in.readVInt();
        entries = new ArrayList<StringEntry>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new StringEntry(in.readUTF(), in.readVLong(), in.readVLong(), in.readDouble(), in.readDouble(), in.readDouble()));
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(name);
        out.writeByte(comparatorType.id());
<<<<<<< HEAD
        out.writeLong(offset);
        out.writeVLong(interval);
        out.writeVInt(size);
        for (int i = 0; i < size; i++) {
            FullEntry entry = (FullEntry) entries[i];
            if (entry == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
//                out.writeLong(entry.key);
                out.writeVLong(entry.count);
                out.writeDouble(entry.min);
                out.writeDouble(entry.max);
                out.writeVLong(entry.totalCount);
                out.writeDouble(entry.total);
            }
        }
        releaseCache();
=======
        out.writeVInt(requiredSize);
        out.writeVLong(missing);

        out.writeVInt(entries.size());
        for (Entry entry : entries) {
            out.writeUTF(entry.term());
            out.writeVLong(entry.count());
            out.writeVLong(entry.totalCount());
            out.writeDouble(entry.total());
            out.writeDouble(entry.min());
            out.writeDouble(entry.max());
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}
