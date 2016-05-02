<<<<<<< HEAD
package redis.clients.jedis;

import static redis.clients.jedis.Protocol.toByteArray;
import static redis.clients.jedis.Protocol.Command.*;
import static redis.clients.jedis.Protocol.Keyword.LIMIT;
import static redis.clients.jedis.Protocol.Keyword.NO;
import static redis.clients.jedis.Protocol.Keyword.ONE;
import static redis.clients.jedis.Protocol.Keyword.STORE;
import static redis.clients.jedis.Protocol.Keyword.WITHSCORES;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import redis.clients.jedis.Protocol.Command;
import redis.clients.jedis.Protocol.Keyword;
import redis.clients.util.SafeEncoder;

public class BinaryClient extends Connection {
    public enum LIST_POSITION {
        BEFORE, AFTER;
        public final byte[] raw;

        private LIST_POSITION() {
            raw = SafeEncoder.encode(name());
        }
    }

    private boolean isInMulti;

    private String password;

    private long db;

    public boolean isInMulti() {
        return isInMulti;
    }

    public BinaryClient(final String host) {
        super(host);
    }

    public BinaryClient(final String host, final int port) {
        super(host, port);
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    @Override
    public void connect() {
        if (!isConnected()) {
            super.connect();
            if (password != null) {
                auth(password);
                getStatusCodeReply();
            }
            if (db > 0) {
                select(Long.valueOf(db).intValue());
                getStatusCodeReply();
            }
        }
    }

    public void ping() {
        sendCommand(PING);
    }

    public void set(final byte[] key, final byte[] value) {
        sendCommand(Command.SET, key, value);
    }

    public void get(final byte[] key) {
        sendCommand(Command.GET, key);
    }

    public void quit() {
        db = 0;
        sendCommand(QUIT);
    }

    public void exists(final byte[] key) {
        sendCommand(EXISTS, key);
    }

    public void del(final byte[]... keys) {
        sendCommand(DEL, keys);
    }

    public void type(final byte[] key) {
        sendCommand(TYPE, key);
    }

    public void flushDB() {
        sendCommand(FLUSHDB);
    }

    public void keys(final byte[] pattern) {
        sendCommand(KEYS, pattern);
    }

    public void randomKey() {
        sendCommand(RANDOMKEY);
    }

    public void rename(final byte[] oldkey, final byte[] newkey) {
        sendCommand(RENAME, oldkey, newkey);
    }

    public void renamenx(final byte[] oldkey, final byte[] newkey) {
        sendCommand(RENAMENX, oldkey, newkey);
    }

    public void dbSize() {
        sendCommand(DBSIZE);
    }

    public void expire(final byte[] key, final int seconds) {
        sendCommand(EXPIRE, key, toByteArray(seconds));
    }

    public void expireAt(final byte[] key, final long unixTime) {
        sendCommand(EXPIREAT, key, toByteArray(unixTime));
    }

    public void ttl(final byte[] key) {
        sendCommand(TTL, key);
    }

    public void select(final int index) {
        db = index;
        sendCommand(SELECT, toByteArray(index));
    }

    public void move(final byte[] key, final int dbIndex) {
        sendCommand(MOVE, key, toByteArray(dbIndex));
    }

    public void flushAll() {
        sendCommand(FLUSHALL);
    }

    public void getSet(final byte[] key, final byte[] value) {
        sendCommand(GETSET, key, value);
    }

    public void mget(final byte[]... keys) {
        sendCommand(MGET, keys);
    }

    public void setnx(final byte[] key, final byte[] value) {
        sendCommand(SETNX, key, value);
    }

    public void setex(final byte[] key, final int seconds, final byte[] value) {
        sendCommand(SETEX, key, toByteArray(seconds), value);
    }

    public void mset(final byte[]... keysvalues) {
        sendCommand(MSET, keysvalues);
    }

    public void msetnx(final byte[]... keysvalues) {
        sendCommand(MSETNX, keysvalues);
    }

    public void decrBy(final byte[] key, final long integer) {
        sendCommand(DECRBY, key, toByteArray(integer));
    }

    public void decr(final byte[] key) {
        sendCommand(DECR, key);
    }

    public void incrBy(final byte[] key, final long integer) {
        sendCommand(INCRBY, key, toByteArray(integer));
    }

    public void incr(final byte[] key) {
        sendCommand(INCR, key);
    }

    public void append(final byte[] key, final byte[] value) {
        sendCommand(APPEND, key, value);
    }

    public void substr(final byte[] key, final int start, final int end) {
        sendCommand(SUBSTR, key, toByteArray(start), toByteArray(end));
    }

    public void hset(final byte[] key, final byte[] field, final byte[] value) {
        sendCommand(HSET, key, field, value);
    }

    public void hget(final byte[] key, final byte[] field) {
        sendCommand(HGET, key, field);
    }

    public void hsetnx(final byte[] key, final byte[] field, final byte[] value) {
        sendCommand(HSETNX, key, field, value);
    }

    public void hmset(final byte[] key, final Map<byte[], byte[]> hash) {
        final List<byte[]> params = new ArrayList<byte[]>();
        params.add(key);

        for (final Entry<byte[], byte[]> entry : hash.entrySet()) {
            params.add(entry.getKey());
            params.add(entry.getValue());
        }
        sendCommand(HMSET, params.toArray(new byte[params.size()][]));
    }

    public void hmget(final byte[] key, final byte[]... fields) {
        final byte[][] params = new byte[fields.length + 1][];
        params[0] = key;
        System.arraycopy(fields, 0, params, 1, fields.length);
        sendCommand(HMGET, params);
    }

    public void hincrBy(final byte[] key, final byte[] field, final long value) {
        sendCommand(HINCRBY, key, field, toByteArray(value));
    }

    public void hexists(final byte[] key, final byte[] field) {
        sendCommand(HEXISTS, key, field);
    }

    public void hdel(final byte[] key, final byte[] field) {
        sendCommand(HDEL, key, field);
    }

    public void hlen(final byte[] key) {
        sendCommand(HLEN, key);
    }

    public void hkeys(final byte[] key) {
        sendCommand(HKEYS, key);
    }

    public void hvals(final byte[] key) {
        sendCommand(HVALS, key);
    }

    public void hgetAll(final byte[] key) {
        sendCommand(HGETALL, key);
    }

    public void rpush(final byte[] key, final byte[] string) {
        sendCommand(RPUSH, key, string);
    }

    public void lpush(final byte[] key, final byte[] string) {
        sendCommand(LPUSH, key, string);
    }

    public void llen(final byte[] key) {
        sendCommand(LLEN, key);
    }

    public void lrange(final byte[] key, final long start, final long end) {
        sendCommand(LRANGE, key, toByteArray(start), toByteArray(end));
    }

    public void ltrim(final byte[] key, final long start, final long end) {
        sendCommand(LTRIM, key, toByteArray(start), toByteArray(end));
    }

    public void lindex(final byte[] key, final long index) {
        sendCommand(LINDEX, key, toByteArray(index));
    }

    public void lset(final byte[] key, final long index, final byte[] value) {
        sendCommand(LSET, key, toByteArray(index), value);
    }

    public void lrem(final byte[] key, long count, final byte[] value) {
        sendCommand(LREM, key, toByteArray(count), value);
    }

    public void lpop(final byte[] key) {
        sendCommand(LPOP, key);
    }

    public void rpop(final byte[] key) {
        sendCommand(RPOP, key);
    }

    public void rpoplpush(final byte[] srckey, final byte[] dstkey) {
        sendCommand(RPOPLPUSH, srckey, dstkey);
    }

    public void sadd(final byte[] key, final byte[] member) {
        sendCommand(SADD, key, member);
    }

    public void smembers(final byte[] key) {
        sendCommand(SMEMBERS, key);
    }

    public void srem(final byte[] key, final byte[] member) {
        sendCommand(SREM, key, member);
    }

    public void spop(final byte[] key) {
        sendCommand(SPOP, key);
    }

    public void smove(final byte[] srckey, final byte[] dstkey,
            final byte[] member) {
        sendCommand(SMOVE, srckey, dstkey, member);
    }

    public void scard(final byte[] key) {
        sendCommand(SCARD, key);
    }

    public void sismember(final byte[] key, final byte[] member) {
        sendCommand(SISMEMBER, key, member);
    }

    public void sinter(final byte[]... keys) {
        sendCommand(SINTER, keys);
    }

    public void sinterstore(final byte[] dstkey, final byte[]... keys) {
        final byte[][] params = new byte[keys.length + 1][];
        params[0] = dstkey;
        System.arraycopy(keys, 0, params, 1, keys.length);
        sendCommand(SINTERSTORE, params);
    }

    public void sunion(final byte[]... keys) {
        sendCommand(SUNION, keys);
    }

    public void sunionstore(final byte[] dstkey, final byte[]... keys) {
        byte[][] params = new byte[keys.length + 1][];
        params[0] = dstkey;
        System.arraycopy(keys, 0, params, 1, keys.length);
        sendCommand(SUNIONSTORE, params);
    }

    public void sdiff(final byte[]... keys) {
        sendCommand(SDIFF, keys);
    }

    public void sdiffstore(final byte[] dstkey, final byte[]... keys) {
        byte[][] params = new byte[keys.length + 1][];
        params[0] = dstkey;
        System.arraycopy(keys, 0, params, 1, keys.length);
        sendCommand(SDIFFSTORE, params);
    }

    public void srandmember(final byte[] key) {
        sendCommand(SRANDMEMBER, key);
    }

    public void zadd(final byte[] key, final double score, final byte[] member) {
        sendCommand(ZADD, key, toByteArray(score), member);
    }

    public void zrange(final byte[] key, final int start, final int end) {
        sendCommand(ZRANGE, key, toByteArray(start), toByteArray(end));
    }

    public void zrem(final byte[] key, final byte[] member) {
        sendCommand(ZREM, key, member);
    }

    public void zincrby(final byte[] key, final double score,
            final byte[] member) {
        sendCommand(ZINCRBY, key, toByteArray(score), member);
    }

    public void zrank(final byte[] key, final byte[] member) {
        sendCommand(ZRANK, key, member);
    }

    public void zrevrank(final byte[] key, final byte[] member) {
        sendCommand(ZREVRANK, key, member);
    }

    public void zrevrange(final byte[] key, final int start, final int end) {
        sendCommand(ZREVRANGE, key, toByteArray(start), toByteArray(end));
    }

    public void zrangeWithScores(final byte[] key, final int start,
            final int end) {
        sendCommand(ZRANGE, key, toByteArray(start), toByteArray(end),
                WITHSCORES.raw);
    }

    public void zrevrangeWithScores(final byte[] key, final int start,
            final int end) {
        sendCommand(ZREVRANGE, key, toByteArray(start), toByteArray(end),
                WITHSCORES.raw);
    }

    public void zcard(final byte[] key) {
        sendCommand(ZCARD, key);
    }

    public void zscore(final byte[] key, final byte[] member) {
        sendCommand(ZSCORE, key, member);
    }

    public void multi() {
        sendCommand(MULTI);
        isInMulti = true;
    }

    public void discard() {
        sendCommand(DISCARD);
        isInMulti = false;
    }

    public void exec() {
        sendCommand(EXEC);
        isInMulti = false;
    }

    public void watch(final byte[]... keys) {
        sendCommand(WATCH, keys);
    }

    public void unwatch() {
        sendCommand(UNWATCH);
    }

    public void sort(final byte[] key) {
        sendCommand(SORT, key);
    }

    public void sort(final byte[] key, final SortingParams sortingParameters) {
        final List<byte[]> args = new ArrayList<byte[]>();
        args.add(key);
        args.addAll(sortingParameters.getParams());
        sendCommand(SORT, args.toArray(new byte[args.size()][]));
    }

    public void blpop(final byte[][] args) {
        sendCommand(BLPOP, args);
    }

    public void sort(final byte[] key, final SortingParams sortingParameters,
            final byte[] dstkey) {
        final List<byte[]> args = new ArrayList<byte[]>();
        args.add(key);
        args.addAll(sortingParameters.getParams());
        args.add(STORE.raw);
        args.add(dstkey);
        sendCommand(SORT, args.toArray(new byte[args.size()][]));
    }

    public void sort(final byte[] key, final byte[] dstkey) {
        sendCommand(SORT, key, STORE.raw, dstkey);
    }

    public void brpop(final byte[][] args) {
        sendCommand(BRPOP, args);
    }

    public void auth(final String password) {
        setPassword(password);
        sendCommand(AUTH, password);
    }

    public void subscribe(final byte[]... channels) {
        sendCommand(SUBSCRIBE, channels);
    }

    public void publish(final byte[] channel, final byte[] message) {
        sendCommand(PUBLISH, channel, message);
    }

    public void unsubscribe() {
        sendCommand(UNSUBSCRIBE);
    }

    public void unsubscribe(final byte[]... channels) {
        sendCommand(UNSUBSCRIBE, channels);
    }

    public void psubscribe(final byte[]... patterns) {
        sendCommand(PSUBSCRIBE, patterns);
    }

    public void punsubscribe() {
        sendCommand(PUNSUBSCRIBE);
    }

    public void punsubscribe(final byte[]... patterns) {
        sendCommand(PUNSUBSCRIBE, patterns);
    }

    public void zcount(final byte[] key, final double min, final double max) {
        sendCommand(ZCOUNT, key, toByteArray(min), toByteArray(max));
    }

    public void zrangeByScore(final byte[] key, final double min,
            final double max) {
        sendCommand(ZRANGEBYSCORE, key, toByteArray(min), toByteArray(max));
    }

    public void zrevrangeByScore(final byte[] key, final double max,
            final double min) {
        sendCommand(ZREVRANGEBYSCORE, key, toByteArray(max), toByteArray(min));
    }

    public void zrangeByScore(final byte[] key, final byte[] min,
            final byte[] max) {
        sendCommand(ZRANGEBYSCORE, key, min, max);
    }

    public void zrevrangeByScore(final byte[] key, final byte[] max,
            final byte[] min) {
        sendCommand(ZREVRANGEBYSCORE, key, max, min);
    }

    public void zrangeByScore(final byte[] key, final double min,
            final double max, final int offset, int count) {
        sendCommand(ZRANGEBYSCORE, key, toByteArray(min), toByteArray(max),
                LIMIT.raw, toByteArray(offset), toByteArray(count));
    }

    public void zrevrangeByScore(final byte[] key, final double max,
            final double min, final int offset, int count) {
        sendCommand(ZREVRANGEBYSCORE, key, toByteArray(max), toByteArray(min),
                LIMIT.raw, toByteArray(offset), toByteArray(count));
    }

    public void zrangeByScoreWithScores(final byte[] key, final double min,
            final double max) {
        sendCommand(ZRANGEBYSCORE, key, toByteArray(min), toByteArray(max),
                WITHSCORES.raw);
    }

    public void zrevrangeByScoreWithScores(final byte[] key, final double max,
            final double min) {
        sendCommand(ZREVRANGEBYSCORE, key, toByteArray(max), toByteArray(min),
                WITHSCORES.raw);
    }

    public void zrangeByScoreWithScores(final byte[] key, final double min,
            final double max, final int offset, final int count) {
        sendCommand(ZRANGEBYSCORE, key, toByteArray(min), toByteArray(max),
                LIMIT.raw, toByteArray(offset), toByteArray(count),
                WITHSCORES.raw);
    }

    public void zrevrangeByScoreWithScores(final byte[] key, final double max,
            final double min, final int offset, final int count) {
        sendCommand(ZREVRANGEBYSCORE, key, toByteArray(max), toByteArray(min),
                LIMIT.raw, toByteArray(offset), toByteArray(count),
                WITHSCORES.raw);
    }

    public void zremrangeByRank(final byte[] key, final int start, final int end) {
        sendCommand(ZREMRANGEBYRANK, key, toByteArray(start), toByteArray(end));
    }

    public void zremrangeByScore(final byte[] key, final double start,
            final double end) {
        sendCommand(ZREMRANGEBYSCORE, key, toByteArray(start), toByteArray(end));
    }

    public void zunionstore(final byte[] dstkey, final byte[]... sets) {
        final byte[][] params = new byte[sets.length + 2][];
        params[0] = dstkey;
        params[1] = toByteArray(sets.length);
        System.arraycopy(sets, 0, params, 2, sets.length);
        sendCommand(ZUNIONSTORE, params);
    }

    public void zunionstore(final byte[] dstkey, final ZParams params,
            final byte[]... sets) {
        final List<byte[]> args = new ArrayList<byte[]>();
        args.add(dstkey);
        args.add(Protocol.toByteArray(sets.length));
        for (final byte[] set : sets) {
            args.add(set);
        }
        args.addAll(params.getParams());
        sendCommand(ZUNIONSTORE, args.toArray(new byte[args.size()][]));
    }

    public void zinterstore(final byte[] dstkey, final byte[]... sets) {
        final byte[][] params = new byte[sets.length + 2][];
        params[0] = dstkey;
        params[1] = Protocol.toByteArray(sets.length);
        System.arraycopy(sets, 0, params, 2, sets.length);
        sendCommand(ZINTERSTORE, params);
    }

    public void zinterstore(final byte[] dstkey, final ZParams params,
            final byte[]... sets) {
        final List<byte[]> args = new ArrayList<byte[]>();
        args.add(dstkey);
        args.add(Protocol.toByteArray(sets.length));
        for (final byte[] set : sets) {
            args.add(set);
        }
        args.addAll(params.getParams());
        sendCommand(ZINTERSTORE, args.toArray(new byte[args.size()][]));
    }

    public void save() {
        sendCommand(SAVE);
    }

    public void bgsave() {
        sendCommand(BGSAVE);
    }

    public void bgrewriteaof() {
        sendCommand(BGREWRITEAOF);
    }

    public void lastsave() {
        sendCommand(LASTSAVE);
    }

    public void shutdown() {
        sendCommand(SHUTDOWN);
    }

    public void info() {
        sendCommand(INFO);
    }

    public void monitor() {
        sendCommand(MONITOR);
    }

    public void slaveof(final String host, final int port) {
        sendCommand(SLAVEOF, host, String.valueOf(port));
    }

    public void slaveofNoOne() {
        sendCommand(SLAVEOF, NO.raw, ONE.raw);
    }

    public void configGet(final String pattern) {
        sendCommand(CONFIG, Keyword.GET.name(), pattern);
    }

    public void configSet(final String parameter, final String value) {
        sendCommand(CONFIG, Keyword.SET.name(), parameter, value);
    }

    public void strlen(final byte[] key) {
        sendCommand(STRLEN, key);
    }

    public void sync() {
        sendCommand(SYNC);
    }

    public void lpushx(final byte[] key, final byte[] string) {
        sendCommand(LPUSHX, key, string);
    }

    public void persist(final byte[] key) {
        sendCommand(PERSIST, key);
    }

    public void rpushx(final byte[] key, final byte[] string) {
        sendCommand(RPUSHX, key, string);
    }

    public void echo(final byte[] string) {
        sendCommand(ECHO, string);
    }

    public void linsert(final byte[] key, final LIST_POSITION where,
            final byte[] pivot, final byte[] value) {
        sendCommand(LINSERT, key, where.raw, pivot, value);
    }

    public void debug(final DebugParams params) {
        sendCommand(DEBUG, params.getCommand());
    }

    public void brpoplpush(final byte[] source, final byte[] destination,
            final int timeout) {
        sendCommand(BRPOPLPUSH, source, destination, toByteArray(timeout));
    }

    public void configResetStat() {
        sendCommand(CONFIG, Keyword.RESETSTAT.name());
    }

    public void setbit(byte[] key, long offset, byte[] value) {
        sendCommand(SETBIT, key, toByteArray(offset), value);
    }

    public void getbit(byte[] key, long offset) {
        sendCommand(GETBIT, key, toByteArray(offset));
    }

    public void setrange(byte[] key, long offset, byte[] value) {
        sendCommand(SETRANGE, key, toByteArray(offset), value);
    }

    public void getrange(byte[] key, long startOffset, long endOffset) {
        sendCommand(GETRANGE, key, toByteArray(startOffset),
                toByteArray(endOffset));
    }

    public Long getDB() {
        return db;
    }

	@Override
    public void disconnect() {
        db = 0;
        super.disconnect();
=======
package nodebox.node;

import nodebox.graphics.Color;
import nodebox.util.waves.*;

import java.util.List;
import java.util.Random;

/**
 * Class containing static method used in Expression.
 *
 * @see nodebox.node.Expression
 */
public class ExpressionHelper {

    // TODO: Expression system is not thread-safe.
    public static ProcessingContext currentContext;
    public static Parameter currentParameter;

    public static Random randomGenerator = new Random();

    public static double random(Object seed, double... minmax) {
        switch (minmax.length) {
            case 0:
                return random(seed);
            case 1:
                return random(seed, minmax[0]);
            default: // Anything larger than 2
                return random(seed, minmax[0], minmax[1]);
        }
    }

    public static double random(Object seed) {
        if (seed instanceof Number) {
            Number number = (Number) seed;
            randomGenerator.setSeed(number.longValue() * 100000000);
        } else {
            randomGenerator.setSeed(seed.hashCode());
        }
        return randomGenerator.nextDouble();
    }

    public static double random(Object seed, double max) {
        return random(seed) * max;
    }

    public static double random(Object seed, double min, double max) {
        return min + random(seed) * (max - min);
    }

    public static int randint(Object seed, int min, int max) {
        if (seed instanceof Number) {
            Number number = (Number) seed;
            randomGenerator.setSeed(number.longValue() * 100000000);
        } else {
            randomGenerator.setSeed(seed.hashCode());
        }
        // nextInt's specified value is exclusive, whereas we want to include it, so add 1.
        return min + randomGenerator.nextInt(max - min + 1);
    }

    public static int toInt(double v) {
        return (int) v;
    }

    public static double toFloat(int v) {
        return (double) v;
    }

    public static double clamp(double v, double min, double max) {
        return min > v ? min : max < v ? max : v;
    }

    public static Color color(double... values) {
        switch (values.length) {
            case 0:
                return new Color();
            case 1:
                return new Color(values[0], values[0], values[0]);
            case 2:
                return new Color(values[0], values[0], values[0], values[1]);
            case 3:
                return new Color(values[0], values[1], values[2]);
            case 4:
                return new Color(values[0], values[1], values[2], values[3]);
            default:
                return new Color();
        }
    }

    public static Color color(double gray) {
        return new Color(gray, gray, gray);
    }

    public static Color color(double gray, double alpha) {
        return new Color(gray, gray, gray, alpha);
    }

    public static Color color(double red, double green, double blue) {
        return new Color(red, green, blue);
    }

    public static Color color(double red, double green, double blue, double alpha) {
        return new Color(red, green, blue, alpha);
    }

    public static Color hsb(double... values) {
        switch (values.length) {
            case 0:
                return new Color();
            case 1:
                return new Color(values[0], values[0], values[0]);
            case 2:
                return new Color(values[0], values[1]);
            case 3:
                return Color.fromHSB(values[0], values[1], values[2]);
            case 4:
                return Color.fromHSB(values[0], values[1], values[2], values[3]);
            default:
                return new Color();
        }
    }

    public static Color hsb(double gray) {
        return new Color(gray, gray, gray);
    }

    public static Color hsb(double gray, double alpha) {
        return new Color(gray, gray, gray, alpha);
    }

    public static Color hsb(double hue, double saturation, double brightness) {
        return Color.fromHSB(hue, saturation, brightness);
    }

    public static Color hsb(double hue, double saturation, double brightness, double alpha) {
        return Color.fromHSB(hue, saturation, brightness, alpha);
    }

    public static double wave(AbstractWave.Type type, double... values) {
        double frame = currentContext.getFrame();

        switch (values.length) {
            case 0:
                return wave(type, 0, 1, 60, frame);
            case 1:
                return wave(type, values[0], 1, 60, frame);
            case 2:
                return wave(type, values[0], values[1], 60, frame);
            case 3:
                return wave(type, values[0], values[2], values[3], frame);
            case 4:
                return wave(type, values[0], values[2], values[3], values[4]);
            default:
                return wave(type, 0, 1, 60, frame);
        }
    }

    public static double wave() {
        return wave(AbstractWave.Type.SINE, 0, 1, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type) {
        return wave(type, 0, 1, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double max) {
        return wave(type, 0, max, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double min, double max) {
        return wave(type, min, max, 60, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double min, double max, double speed) {
        return wave(type, min, max, speed, currentContext.getFrame());
    }

    public static double wave(AbstractWave.Type type, double min, double max, double speed, double frame) {
        float fmin = (float) min;
        float fmax = (float) max;
        float fspeed = (float) speed;

        AbstractWave wave;
        switch (type) {
            case TRIANGLE:
                wave = TriangleWave.from(fmin, fmax, fspeed);
                break;
            case SQUARE:
                wave = SquareWave.from(fmin, fmax, fspeed);
                break;
            case SAWTOOTH:
                wave = SawtoothWave.from(fmin, fmax, fspeed);
                break;
            case SINE:
            default:
                wave = SineWave.from(fmin, fmax, fspeed);
                break;
        }
        return wave.getValueAt((float) frame);
    }

    public static double hold(double minFrame, double functionValue, double... values) {
        double frame = currentContext.getFrame();

        switch (values.length) {
            case 1:
                return hold(minFrame, functionValue, values[0], frame);
            case 2:
                return hold(minFrame, functionValue, values[0], values[1]);
            case 0:
            default:
                return hold(minFrame, functionValue, 0, frame);
        }
    }

    public static double hold(double minFrame, double functionValue) {
        return hold(minFrame, functionValue, 0, currentContext.getFrame());
    }

    public static double hold(double minFrame, double functionValue, double defaultValue) {
        return hold(minFrame, functionValue, defaultValue, currentContext.getFrame());
    }

    public static double hold(double minFrame, double functionValue, double defaultValue, double frame) {
        return frame < minFrame ? defaultValue : functionValue;
    }

    public static double schedule(double start, double end, double functionValue, double... values) {
        double frame = currentContext.getFrame();

        switch (values.length) {
            case 1:
                return schedule(start, end, functionValue, values[0], frame);
            case 2:
                return schedule(start, end, functionValue, values[0], values[1]);
            case 0:
            default:
                return schedule(start, end, functionValue, 0, frame);
        }
    }

    public static double schedule(double start, double end, double functionValue) {
        return schedule(start, end, functionValue, 0, currentContext.getFrame());
    }

    public static double schedule(double start, double end, double functionValue, double defaultValue) {
        return schedule(start, end, functionValue, defaultValue, currentContext.getFrame());
    }

    public static double schedule(double start, double end, double functionValue, double defaultValue, double frame) {
        return start <= frame && frame < end ? functionValue : defaultValue;
    }

    public static double timeloop(double speed, List<Number> values) {
        return timeloop(speed, values, currentContext.getFrame());
    }

    public static double timeloop(double speed, List<Number> values, double frame) {
        if (values.size() == 0) return 0;
        int index = (int) (frame / speed);
        try {
            return values.get(index % values.size()).doubleValue();
        } catch (ClassCastException e) {
            return 0;
        }
    }

    public static Object stamp(String key, Object defaultValue) {
        if (currentContext == null) return defaultValue;
        currentParameter.markStampExpression();
        Object v = currentContext.get(key);
        return v != null ? v : defaultValue;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

