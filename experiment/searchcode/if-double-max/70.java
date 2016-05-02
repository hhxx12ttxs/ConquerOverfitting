<<<<<<< HEAD
package redis.clients.jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;

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
            formatted.add(generateResponse(o).get());
        }
        return formatted;
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

    public Response<String> get(byte[] key) {
        client.get(key);
        return getResponse(BuilderFactory.STRING);
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

    public Response<String> hget(byte[] key, byte[] field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Map<String, String>> hgetAll(byte[] key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.STRING_MAP);
    }

    public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> hkeys(byte[] key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> hlen(byte[] key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hmget(byte[] key, byte[]... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<String> hmset(byte[] key, Map<byte[], byte[]> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hvals(byte[] key) {
        client.hvals(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> incr(byte[] key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(byte[] key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> keys(byte[] pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<String> lindex(byte[] key, int index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.STRING);
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

    public Response<String> lpop(byte[] key) {
        client.lpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> lpush(byte[] key, byte[] string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(byte[] key, byte[] bytes) {
        client.lpushx(key, bytes);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> lrange(byte[] key, int start, int end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> lrem(byte[] key, int count, byte[] value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lset(byte[] key, int index, byte[] value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(byte[] key, int start, int end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<List<String>> mget(byte[]... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.STRING_LIST);
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

    public Response<String> rpop(byte[] key) {
        client.rpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rpoplpush(byte[] srckey, byte[] dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.STRING);
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

    public Response<Set<String>> sdiff(byte[]... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> set(byte[] key, byte[] value) {
        client.set(key, value);
        return getResponse(BuilderFactory.STRING);
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

    public Response<Set<String>> sinter(byte[]... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> sismember(byte[] key, byte[] member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Set<String>> smembers(byte[] key) {
        client.smembers(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> sort(byte[] key) {
        client.sort(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key,
            SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key,
            SortingParams sortingParameters, byte[] dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key, byte[] dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<String> spop(byte[] key) {
        client.spop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> srandmember(byte[] key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> srem(byte[] key, byte[] member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(byte[] key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> substr(byte[] key, int start, int end) {
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Set<String>> sunion(byte[]... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.STRING_SET);
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

    public Response<String> watch(byte[]... keys) {
        client.watch(keys);
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

    public Response<Set<String>> zrange(byte[] key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, double min,
            double max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, byte[] min,
            byte[] max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, double min,
            double max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeWithScores(byte[] key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
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

    public Response<Set<String>> zrevrange(byte[] key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(byte[] key, int start,
            int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
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

    public Response<String> brpoplpush(byte[] source, byte[] destination,
            int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.STRING);
    }
}
=======
/*
 The MIT License

 Copyright (c) 2010-2014 Paul R. Holser, Jr.

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package com.pholser.junit.quickcheck.random;

import java.math.BigInteger;
import java.util.Random;

import com.pholser.junit.quickcheck.internal.Ranges;

import static com.pholser.junit.quickcheck.internal.Ranges.*;

/**
 * A source of randomness, fed to {@link com.pholser.junit.quickcheck.generator.Generator}s so they can produce
 * random values for theory parameters.
 */
public class SourceOfRandomness {
    private final Random delegate;

    /**
     * Makes a new source of randomness.
     *
     * @param delegate a JDK source of randomness, to which the new instance will delegate
     */
    public SourceOfRandomness(Random delegate) {
        this.delegate = delegate;
    }

    /**
     * @return a uniformly distributed boolean value
     * @see java.util.Random#nextBoolean()
     */
    public boolean nextBoolean() {
        return delegate.nextBoolean();
    }

    /**
     * @param bytes a byte array to fill with random values
     * @see java.util.Random#nextBytes(byte[])
     */
    public void nextBytes(byte[] bytes) {
        delegate.nextBytes(bytes);
    }

    /**
     * Gives an array of a given length containing random bytes.
     *
     * @param count the desired length of the random byte array
     * @return random bytes
     * @see java.util.Random#nextBytes(byte[])
     */
    public byte[] nextBytes(int count) {
        byte[] buffer = new byte[count];
        delegate.nextBytes(buffer);
        return buffer;
    }

    /**
     * @return a uniformly distributed random {@code double} value in the interval {@code [0.0, 1.0)}
     * @see java.util.Random#nextDouble()
     */
    public double nextDouble() {
        return delegate.nextDouble();
    }

    /**
     * @return a uniformly distributed random {@code float} value in the interval {@code [0.0, 1.0)}
     * @see java.util.Random#nextFloat()
     */
    public float nextFloat() {
        return delegate.nextFloat();
    }

    /**
     * @return a Gaussian-distributed random double value
     * @see java.util.Random#nextGaussian()
     */
    public double nextGaussian() {
        return delegate.nextGaussian();
    }

    /**
     * @return a uniformly distributed random {@code int} value
     * @see java.util.Random#nextInt()
     */
    public int nextInt() {
        return delegate.nextInt();
    }

    /**
     * @param n upper bound
     * @return a uniformly distributed random {@code int} value in the interval {@code [0, n)}
     * @see java.util.Random#nextInt(int)
     */
    public int nextInt(int n) {
        return delegate.nextInt(n);
    }

    /**
     * @return a uniformly distributed random {@code long} value
     * @see java.util.Random#nextLong()
     */
    public long nextLong() {
        return delegate.nextLong();
    }

    /**
     * @param seed value with which to seed this source of randomness
     * @see java.util.Random#setSeed(long)
     */
    public void setSeed(long seed) {
        delegate.setSeed(seed);
    }

    /**
     * Gives a random {@code byte} value, uniformly distributed across the interval {@code [min, max]}.
     *
     * @param min lower bound of the desired interval
     * @param max upper bound of the desired interval
     * @return a random value
     */
    public byte nextByte(byte min, byte max) {
        return (byte) nextLong(min, max);
    }

    /**
     * Gives a random {@code char} value, uniformly distributed across the interval {@code [min, max]}.
     *
     * @param min lower bound of the desired interval
     * @param max upper bound of the desired interval
     * @return a random value
     */
    public char nextChar(char min, char max) {
        checkRange(Ranges.Type.CHARACTER, min, max);

        return (char) nextLong(min, max);
    }

    /**
     * <p>Gives a random {@code double} value in the interval {@code [min, max]}.</p>
     *
     * <p>This naive implementation takes a random {@code double} value from {@link Random#nextDouble()} and
     * scales/shifts the value into the desired interval. This may give surprising results for large ranges.</p>
     *
     * @param min lower bound of the desired interval
     * @param max upper bound of the desired interval
     * @return a random value
     */
    public double nextDouble(double min, double max) {
        int comparison = checkRange(Ranges.Type.FLOAT, min, max);
        return comparison == 0 ? min : min + (max - min) * nextDouble();
    }

    /**
     * <p>Gives a random {@code float} value in the interval {@code [min, max]}.</p>
     *
     * <p>This naive implementation takes a random {@code float} value from {@link Random#nextFloat()} and
     * scales/shifts the value into the desired interval. This may give surprising results for large ranges.</p>
     *
     * @param min lower bound of the desired interval
     * @param max upper bound of the desired interval
     * @return a random value
     */
    public float nextFloat(float min, float max) {
        int comparison = checkRange(Ranges.Type.FLOAT, min, max);
        return comparison == 0 ? min : min + (max - min) * nextFloat();
    }

    /**
     * Gives a random {@code int} value, uniformly distributed across the interval {@code [min, max]}.
     *
     * @param min lower bound of the desired interval
     * @param max upper bound of the desired interval
     * @return a random value
     */
    public int nextInt(int min, int max) {
        return (int) nextLong(min, max);
    }

    /**
     * Gives a random {@code long} value, uniformly distributed across the interval {@code [min, max]}.
     *
     * @param min lower bound of the desired interval
     * @param max upper bound of the desired interval
     * @return a random value
     */
    public long nextLong(long min, long max) {
        int comparison = checkRange(Ranges.Type.INTEGRAL, min, max);
        if (comparison == 0)
            return min;

        return Ranges.choose(this, BigInteger.valueOf(min), BigInteger.valueOf(max)).longValue();
    }

    /**
     * Gives a random {@code short} value, uniformly distributed across the interval {@code [min, max]}.
     *
     * @param min lower bound of the desired interval
     * @param max upper bound of the desired interval
     * @return a random value
     */
    public short nextShort(short min, short max) {
        return (short) nextLong(min, max);
    }

    /**
     * Gives a random {@code BigInteger} representable by the given number of bits.
     *
     * @param numberOfBits the desired number of bits
     * @return a random {@code BigInteger}
     * @see BigInteger#BigInteger(int, java.util.Random)
     */
    public BigInteger nextBigInteger(int numberOfBits) {
        return new BigInteger(numberOfBits, delegate);
    }
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
