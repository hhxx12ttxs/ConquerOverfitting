<<<<<<< HEAD
package redis.clients.jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.exceptions.JedisDataException;

public class BinaryTransaction extends Queable {
    protected Client client = null;
    protected boolean inTransaction = true;

    public BinaryTransaction() {
    }

    public BinaryTransaction(final Client client) {
        this.client = client;
    }

    public List<Object> exec() {
        client.exec();
        client.getAll(1); // Discard all but the last reply

        List<Object> unformatted = client.getObjectMultiBulkReply();
        if (unformatted == null) {
            return null;
        }
        List<Object> formatted = new ArrayList<Object>();
        for (Object o : unformatted) {
        	try{
        		formatted.add(generateResponse(o).get());
        	}catch(JedisDataException e){
        		formatted.add(e);
        	}
        }
        return formatted;
    }
    
    public List<Response<?>> execGetResponse() {
        client.exec();
        client.getAll(1); // Discard all but the last reply

        List<Object> unformatted = client.getObjectMultiBulkReply();
        if (unformatted == null) {
            return null;
        }
        List<Response<?>> response = new ArrayList<Response<?>>();
        for (Object o : unformatted) {
        	response.add(generateResponse(o));
        }
        return response;
    }

    public String discard() {
        client.discard();
        client.getAll(1); // Discard all but the last reply
        inTransaction = false;
        clean();
        return client.getStatusCodeReply();
    }

    public Response<Long> append(byte[] key, byte[] value) {
        client.append(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> blpop(byte[]... args) {
        client.blpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> brpop(byte[]... args) {
        client.brpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> decr(byte[] key) {
        client.decr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decrBy(byte[] key, long integer) {
        client.decrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(byte[]... keys) {
        client.del(keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> echo(byte[] string) {
        client.echo(string);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Boolean> exists(byte[] key) {
        client.exists(key);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Long> expire(byte[] key, int seconds) {
        client.expire(key, seconds);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expireAt(byte[] key, long unixTime) {
        client.expireAt(key, unixTime);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<byte[]> get(byte[] key) {
        client.get(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<String> getSet(byte[] key, byte[] value) {
        client.getSet(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> hdel(byte[] key, byte[] field) {
        client.hdel(key, field);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> hexists(byte[] key, byte[] field) {
        client.hexists(key, field);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<byte[]> hget(byte[] key, byte[] field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Map<String, String>> hgetAll(byte[] key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.STRING_MAP);
    }

    public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<byte[]>> hkeys(byte[] key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> hlen(byte[] key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<byte[]>> hmget(byte[] key, byte[]... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<byte[]> hmset(byte[] key, Map<byte[], byte[]> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<byte[]>> hvals(byte[] key) {
        client.hvals(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<Long> incr(byte[] key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(byte[] key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<byte[]>> keys(byte[] pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<byte[]> lindex(byte[] key, long index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> linsert(byte[] key, LIST_POSITION where,
            byte[] pivot, byte[] value) {
        client.linsert(key, where, pivot, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> llen(byte[] key) {
        client.llen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<byte[]> lpop(byte[] key) {
        client.lpop(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> lpush(byte[] key, byte[] string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(byte[] key, byte[] bytes) {
        client.lpushx(key, bytes);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<byte[]>> lrange(byte[] key, long start, long end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<Long> lrem(byte[] key, long count, byte[] value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lset(byte[] key, long index, byte[] value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(byte[] key, long start, long end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<List<byte[]>> mget(byte[]... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<Long> move(byte[] key, int dbIndex) {
        client.move(key, dbIndex);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> mset(byte[]... keysvalues) {
        client.mset(keysvalues);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> msetnx(byte[]... keysvalues) {
        client.msetnx(keysvalues);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> persist(byte[] key) {
        client.persist(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> rename(byte[] oldkey, byte[] newkey) {
        client.rename(oldkey, newkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> renamenx(byte[] oldkey, byte[] newkey) {
        client.renamenx(oldkey, newkey);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<byte[]> rpop(byte[] key) {
        client.rpop(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<byte[]> rpoplpush(byte[] srckey, byte[] dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> rpush(byte[] key, byte[] string) {
        client.rpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpushx(byte[] key, byte[] string) {
        client.rpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sadd(byte[] key, byte[] member) {
        client.sadd(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> scard(byte[] key) {
        client.scard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<byte[]>> sdiff(byte[]... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<byte[]> set(byte[] key, byte[] value) {
        client.set(key, value);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Boolean> setbit(String key, long offset, boolean value) {
        client.setbit(key, offset, value);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> setex(byte[] key, int seconds, byte[] value) {
        client.setex(key, seconds, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> setnx(byte[] key, byte[] value) {
        client.setnx(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<byte[]>> sinter(byte[]... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> sismember(byte[] key, byte[] member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Set<byte[]>> smembers(byte[] key) {
        client.smembers(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<byte[]>> sort(byte[] key) {
        client.sort(key);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<byte[]>> sort(byte[] key,
            SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<byte[]>> sort(byte[] key,
            SortingParams sortingParameters, byte[] dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<List<byte[]>> sort(byte[] key, byte[] dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.BYTE_ARRAY_LIST);
    }

    public Response<byte[]> spop(byte[] key) {
        client.spop(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<byte[]> srandmember(byte[] key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> srem(byte[] key, byte[] member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(byte[] key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> substr(byte[] key, int start, int end) { // what's
        // that?
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Set<byte[]>> sunion(byte[]... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Long> sunionstore(byte[] dstkey, byte[]... keys) {
        client.sunionstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> ttl(byte[] key) {
        client.ttl(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> type(byte[] key) {
        client.type(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> zadd(byte[] key, double score, byte[] member) {
        client.zadd(key, score, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcard(byte[] key) {
        client.zcard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcount(byte[] key, double min, double max) {
        client.zcount(key, min, max);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zincrby(byte[] key, double score, byte[] member) {
        client.zincrby(key, score, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zinterstore(byte[] dstkey, byte[]... sets) {
        client.zinterstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(byte[] dstkey, ZParams params,
            byte[]... sets) {
        client.zinterstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<byte[]>> zrange(byte[] key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<byte[]>> zrangeByScore(byte[] key, double min,
            double max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<byte[]>> zrangeByScore(byte[] key, byte[] min,
            byte[] max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<byte[]>> zrangeByScore(byte[] key, double min,
            double max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Set<Tuple>> zrangeWithScores(byte[] key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Long> zrank(byte[] key, byte[] member) {
        client.zrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrem(byte[] key, byte[] member) {
        client.zrem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByRank(byte[] key, int start, int end) {
        client.zremrangeByRank(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(byte[] key, double start, double end) {
        client.zremrangeByScore(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<byte[]>> zrevrange(byte[] key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.BYTE_ARRAY_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(byte[] key, int start,
            int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET_BINARY);
    }

    public Response<Long> zrevrank(byte[] key, byte[] member) {
        client.zrevrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zscore(byte[] key, byte[] member) {
        client.zscore(key, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zunionstore(byte[] dstkey, byte[]... sets) {
        client.zunionstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(byte[] dstkey, ZParams params,
            byte[]... sets) {
        client.zunionstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<byte[]> brpoplpush(byte[] source, byte[] destination,
            int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }
}

=======
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

package org.elasticsearch.search.facet.datehistogram;

import org.elasticsearch.common.CacheRecycler;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.trove.ExtTLongObjectHashMap;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.facet.Facet;

import java.io.IOException;
import java.util.*;

/**
 * @author kimchy (shay.banon)
 */
public class InternalFullDateHistogramFacet extends InternalDateHistogramFacet {

    private static final String STREAM_TYPE = "fdHistogram";

    public static void registerStreams() {
        Streams.registerStream(STREAM, STREAM_TYPE);
    }

    static Stream STREAM = new Stream() {
        @Override public Facet readFacet(String type, StreamInput in) throws IOException {
            return readHistogramFacet(in);
        }
    };

    @Override public String streamType() {
        return STREAM_TYPE;
    }


    /**
     * A histogram entry representing a single entry within the result of a histogram facet.
     */
    public static class FullEntry implements Entry {
        private final long time;
        long count;
        long totalCount;
        double total;
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        public FullEntry(long time, long count, double min, double max, long totalCount, double total) {
            this.time = time;
            this.count = count;
            this.min = min;
            this.max = max;
            this.totalCount = totalCount;
            this.total = total;
        }

        @Override public long time() {
            return time;
        }

        @Override public long getTime() {
            return time();
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
        }

        @Override public long getTotalCount() {
            return this.totalCount;
        }

        @Override public double mean() {
            return total / totalCount;
        }

        @Override public double getMean() {
            return total / totalCount;
        }

        @Override public double min() {
            return this.min;
        }

        @Override public double getMin() {
            return this.min;
        }

        @Override public double max() {
            return this.max;
        }

        @Override public double getMax() {
            return this.max;
        }
    }

    private String name;

    private ComparatorType comparatorType;

    ExtTLongObjectHashMap<InternalFullDateHistogramFacet.FullEntry> tEntries;
    boolean cachedEntries;
    Collection<FullEntry> entries;

    private InternalFullDateHistogramFacet() {
    }

    public InternalFullDateHistogramFacet(String name, ComparatorType comparatorType, ExtTLongObjectHashMap<InternalFullDateHistogramFacet.FullEntry> entries, boolean cachedEntries) {
        this.name = name;
        this.comparatorType = comparatorType;
        this.tEntries = entries;
        this.cachedEntries = cachedEntries;
        this.entries = entries.valueCollection();
    }

    @Override public String name() {
        return this.name;
    }

    @Override public String getName() {
        return name();
    }

    @Override public String type() {
        return TYPE;
    }

    @Override public String getType() {
        return type();
    }

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
    }

    @Override public Facet reduce(String name, List<Facet> facets) {
        if (facets.size() == 1) {
            // we need to sort it
            InternalFullDateHistogramFacet internalFacet = (InternalFullDateHistogramFacet) facets.get(0);
            List<FullEntry> entries = internalFacet.entries();
            Collections.sort(entries, comparatorType.comparator());
            internalFacet.releaseCache();
            return internalFacet;
        }

        ExtTLongObjectHashMap<FullEntry> map = CacheRecycler.popLongObjectMap();

        for (Facet facet : facets) {
            InternalFullDateHistogramFacet histoFacet = (InternalFullDateHistogramFacet) facet;
            for (FullEntry fullEntry : histoFacet.entries) {
                FullEntry current = map.get(fullEntry.time);
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
                    map.put(fullEntry.time, fullEntry);
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
        InternalFullDateHistogramFacet ret = new InternalFullDateHistogramFacet();
        ret.name = name;
        ret.comparatorType = comparatorType;
        ret.entries = ordered;
        return ret;
    }

    static final class Fields {
        static final XContentBuilderString _TYPE = new XContentBuilderString("_type");
        static final XContentBuilderString ENTRIES = new XContentBuilderString("entries");
        static final XContentBuilderString TIME = new XContentBuilderString("time");
        static final XContentBuilderString COUNT = new XContentBuilderString("count");
        static final XContentBuilderString TOTAL = new XContentBuilderString("total");
        static final XContentBuilderString TOTAL_COUNT = new XContentBuilderString("total_count");
        static final XContentBuilderString MEAN = new XContentBuilderString("mean");
        static final XContentBuilderString MIN = new XContentBuilderString("min");
        static final XContentBuilderString MAX = new XContentBuilderString("max");
    }

    @Override public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(name);
        builder.field(Fields._TYPE, TYPE);
        builder.startArray(Fields.ENTRIES);
        for (Entry entry : entries()) {
            builder.startObject();
            builder.field(Fields.TIME, entry.time());
            builder.field(Fields.COUNT, entry.count());
            builder.field(Fields.MIN, entry.min());
            builder.field(Fields.MAX, entry.max());
            builder.field(Fields.TOTAL, entry.total());
            builder.field(Fields.TOTAL_COUNT, entry.totalCount());
            builder.field(Fields.MEAN, entry.mean());
            builder.endObject();
        }
        builder.endArray();
        builder.endObject();
        return builder;
    }

    public static InternalFullDateHistogramFacet readHistogramFacet(StreamInput in) throws IOException {
        InternalFullDateHistogramFacet facet = new InternalFullDateHistogramFacet();
        facet.readFrom(in);
        return facet;
    }

    @Override public void readFrom(StreamInput in) throws IOException {
        name = in.readUTF();
        comparatorType = ComparatorType.fromId(in.readByte());

        cachedEntries = false;
        int size = in.readVInt();
        entries = new ArrayList<FullEntry>(size);
        for (int i = 0; i < size; i++) {
            entries.add(new FullEntry(in.readLong(), in.readVLong(), in.readDouble(), in.readDouble(), in.readVLong(), in.readDouble()));
        }
    }

    @Override public void writeTo(StreamOutput out) throws IOException {
        out.writeUTF(name);
        out.writeByte(comparatorType.id());
        out.writeVInt(entries.size());
        for (FullEntry entry : entries) {
            out.writeLong(entry.time);
            out.writeVLong(entry.count);
            out.writeDouble(entry.min);
            out.writeDouble(entry.max);
            out.writeVLong(entry.totalCount);
            out.writeDouble(entry.total);
        }
        releaseCache();
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
