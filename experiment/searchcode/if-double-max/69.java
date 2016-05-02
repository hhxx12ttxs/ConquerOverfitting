<<<<<<< HEAD
package ch.idsia.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

/**
 * This class is used to model the statistics
 * of a fix of numbers.  For the statistics
 * we choose here it is not necessary to store
 * all the numbers - just keeping a running total
 * of how many, the sum and the sum of the squares
 * is sufficient (plus max and min, for max and min).
 * <p/>
 * Warning: the geometric mean calculation is only valid if all numbers
 * added to the summary are positive (>0) - no warnings
 * are given if this is not the case - you'll just get a wrong answer
 * for the gm() !!!
 */

public class StatisticalSummary implements java.io.Serializable {

    // a temporary fix for an immediate need
    // this should really be handled with a more general
    // predicate class

    public static class Watch {
        double x;
        public int count;

        public Watch(double x) {
            this.x = x;
            count = 0;
        }

        public void note(double val) {
            if (val == x) {
                count++;
            }
        }

        public String toString() {
            return x + " occured " + count + " times ";
        }

        public void reset() {
            count = 0;
        }
    }

    // following line can cause prog to hang - bug in Java?
    // protected long serialVersionUID = new Double("-1490108905720833569").longValue();
    // protected long serialVersionUID = 123;
    public String name; // defaults to ""
    private double logsum; // for calculating the geometric mean
    private double sum;
    private double sumsq;
    private double min;
    private double max;

    private double mean;
    private double gm; // geometric mean
    private double sd;

    // trick class loader into loading this now
    // private static StatisticalTests dummy = new StatisticalTests();

    int n;
    boolean valid;
    public Watch watch;

    public StatisticalSummary() {
        this("");
        // System.out.println("Exited default...");
    }

    public StatisticalSummary(String name) {
        // System.out.println("Creating SS");
        this.name = name;
        n = 0;
        sum = 0;
        sumsq = 0;
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        // System.out.println("Finished Creating SS");
        watch = null;
        valid = false;
    }

    public final void reset() {
        n = 0;
        sum = 0;
        sumsq = 0;
        logsum = 0;
        // ensure that the first number to be
        // added will fix up min and max to
        // be that number
        min = Double.POSITIVE_INFINITY;
        max = Double.NEGATIVE_INFINITY;
        if (watch != null) {
            watch.reset();
        }
    }


    public double max() {
        return max;
    }

    public double min() {
        return min;
    }

    public double mean() {
        if (!valid)
            computeStats();
        return mean;
    }

    public double gm() {
        if (!valid)
            computeStats();
        return gm;
    }
/* erroneous
  public static double sigDiff( StatisticalSummary s1 , StatisticalSummary s2 ) {
    return StatisticalTests.tNotPaired(
      s1.mean(), s2.mean(), s1.sumsq, s2.sumsq, s1.n, s2.n, true);
  }
*/

    public static double sigDiff(StatisticalSummary s1, StatisticalSummary s2) {
        return StatisticalTests.tNotPaired(
                s1.mean(), s2.mean(), s1.sumSquareDiff(), s2.sumSquareDiff(), s1.n, s2.n, true);
    }

    /**
     * returns the sum of the squares of the differences
     * between the mean and the ith values
     */
    public double sumSquareDiff() {
        return sumsq - n * mean() * mean();
    }

    private void computeStats() {
        if (!valid) {
            mean = sum / n;
            gm = Math.exp(logsum / n);
            double num = sumsq - (n * mean * mean);
            if (num < 0) {
                // avoids tiny negative numbers possible through imprecision
                num = 0;
            }
            // System.out.println("Num = " + num);
            sd = Math.sqrt(num / (n - 1));
            // System.out.println(" Test: sd = " + sd);
            // System.out.println(" Test: n = " + n);
            valid = true;
        }
    }

    public double sd() {
        if (!valid)
            computeStats();
        return sd;
    }

    public int n() {
        return n;
    }

    public double stdErr() {
        return sd() / Math.sqrt(n);
    }

    public void add(StatisticalSummary ss) {
        // implications for Watch?
        n += ss.n;
        sum += ss.sum;
        sumsq += ss.sumsq;
        logsum += ss.logsum;
        max = Math.max(max, ss.max);
        min = Math.min(min, ss.min);
        valid = false;
    }

    public void add(double d) {
        n++;
        sum += d;
        sumsq += d * d;
        if (d > 0) {
            logsum += Math.log(d);
        }
        min = Math.min(min, d);
        max = Math.max(max, d);
        if (watch != null) {
            watch.note(d);
        }
        valid = false;
    }

    public void add(Number n) {
        add(n.doubleValue());
    }

    public void add(double[] d) {
        for (int i = 0; i < d.length; i++) {
            add(d[i]);
        }
    }

    public void add(Vector v) {
        for (int i = 0; i < v.size(); i++) {
            try {
                add(((Number) v.elementAt(i)).doubleValue());
            } catch (Exception e) {
            }
        }
    }

    public String toString() {
        String s = (name == null) ? "" : name + "\n";
        s += " min = " + min() + "\n" +
                " max = " + max() + "\n" +
                " ave = " + mean() + "\n" +
                " sd  = " + sd() + "\n" +
               // " se  = " + stdErr() + "\n" +
               // " sum  = " + sum + "\n" +
               // " sumsq  = " + sumsq + "\n" +
               // " watch = " + watch + "\n" +
                " n   = " + n;
        return s;

    }

    public void save(String path) {
        try {
            ObjectOutputStream oos =
                    new ObjectOutputStream(
                            new FileOutputStream(path));
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static StatisticalSummary load(String path) {
        try {
            ObjectInputStream ois =
                    new ObjectInputStream(
                            new FileInputStream(path));
            StatisticalSummary ss = (StatisticalSummary) ois.readObject();
            ois.close();
            return ss;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }


    public static void main(String[] args) throws Exception {
        // demonstrate some possible usage...

        StatisticalSummary ts1 = new StatisticalSummary();
        StatisticalSummary ts2 = new StatisticalSummary();
        for (int i = 0; i < 100; i++) {
            ts1.add(i / 10);
            ts2.add(i / 10 + new Double(args[0]).doubleValue());
        }

        System.out.println(ts1);
        System.out.println(ts2);
        System.out.println(StatisticalSummary.sigDiff(ts1, ts2));
        System.out.println((ts2.mean() - ts1.mean()) / ts1.stdErr());

        System.exit(0);

        System.out.println("Creating summaries");

        StatisticalSummary trainSummary = new StatisticalSummary();
        System.out.println("1");
        // StatisticalSummary testSummary = new VisualSummary("EA");
        System.out.println("2");
        // testSummary.watch = new StatisticalSummary.Watch( 1.0 );
        System.out.println("3");
        // StatisticalSummary ostiaTrainSummary = new StatisticalSummary();
        System.out.println("4");
        // ostiaTestSummary = new VisualSummary("OSTIA");
        System.out.println("5");
        // ostiaTestSummary.watch = new StatisticalSummary.Watch( 1.0 );

        System.out.println("Created summaries");


        StatisticalSummary s10 = new StatisticalSummary();
        StatisticalSummary s20 = new StatisticalSummary();
        StatisticalSummary s3 = new StatisticalSummary();
        StatisticalSummary s4 = new StatisticalSummary();
        StatisticalSummary s5 = new StatisticalSummary();
        StatisticalSummary ss = new StatisticalSummary("Hello");
        for (int i = 0; i < 20; i++) {
            ss.add(0.71);
        }
        System.out.println(ss);
        System.exit(0);

        StatisticalSummary s1 = new StatisticalSummary();
        StatisticalSummary s2 = new StatisticalSummary();

        System.out.println(sigDiff(s1, s2));

        for (int i = 0; i < 20; i++) {
            s1.add(Math.random());
            s2.add(Math.random() + 0.5);
            // s1.add(i);
            // s2.add(i+2);
            System.out.println(sigDiff(s1, s2));
        }
    }
}

=======
package redis.clients.jedis;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Jedis {
    private Client client = null;

    public Jedis(String host) {
	client = new Client(host);
    }

    public Jedis(String host, int port) {
	client = new Client(host, port);
    }

    public Jedis(String host, int port, int timeout) {
	client = new Client(host, port);
	client.setTimeout(timeout);
    }

    public String ping() {
	checkIsInMulti();
	client.ping();
	return client.getStatusCodeReply();
    }

    public String set(String key, String value) {
	checkIsInMulti();
	client.set(key, value);
	return client.getStatusCodeReply();
    }

    public String get(String key) {
	checkIsInMulti();
	client.sendCommand("GET", key);
	return client.getBulkReply();
    }

    public void quit() {
	checkIsInMulti();
	client.quit();
    }

    public int exists(String key) {
	checkIsInMulti();
	client.exists(key);
	return client.getIntegerReply();
    }

    public int del(String... keys) {
	checkIsInMulti();
	client.del(keys);
	return client.getIntegerReply();
    }

    public String type(String key) {
	checkIsInMulti();
	client.type(key);
	return client.getStatusCodeReply();
    }

    public String flushDB() {
	checkIsInMulti();
	client.flushDB();
	return client.getStatusCodeReply();
    }

    public List<String> keys(String pattern) {
	checkIsInMulti();
	client.keys(pattern);
	return client.getMultiBulkReply();
    }

    public String randomKey() {
	checkIsInMulti();
	client.randomKey();
	return client.getBulkReply();
    }

    public String rename(String oldkey, String newkey) {
	checkIsInMulti();
	client.rename(oldkey, newkey);
	return client.getStatusCodeReply();
    }

    public int renamenx(String oldkey, String newkey) {
	checkIsInMulti();
	client.renamenx(oldkey, newkey);
	return client.getIntegerReply();
    }

    public int dbSize() {
	checkIsInMulti();
	client.dbSize();
	return client.getIntegerReply();
    }

    public int expire(String key, int seconds) {
	checkIsInMulti();
	client.expire(key, seconds);
	return client.getIntegerReply();
    }

    public int expireAt(String key, long unixTime) {
	checkIsInMulti();
	client.expireAt(key, unixTime);
	return client.getIntegerReply();
    }

    public int ttl(String key) {
	checkIsInMulti();
	client.ttl(key);
	return client.getIntegerReply();
    }

    public String select(int index) {
	checkIsInMulti();
	client.select(index);
	return client.getStatusCodeReply();
    }

    public int move(String key, int dbIndex) {
	checkIsInMulti();
	client.move(key, dbIndex);
	return client.getIntegerReply();
    }

    public String flushAll() {
	checkIsInMulti();
	client.flushAll();
	return client.getStatusCodeReply();
    }

    public String getSet(String key, String value) {
	checkIsInMulti();
	client.getSet(key, value);
	return client.getBulkReply();
    }

    public List<String> mget(String... keys) {
	checkIsInMulti();
	client.mget(keys);
	return client.getMultiBulkReply();
    }

    public int setnx(String key, String value) {
	checkIsInMulti();
	client.setnx(key, value);
	return client.getIntegerReply();
    }

    public String setex(String key, int seconds, String value) {
	checkIsInMulti();
	client.setex(key, seconds, value);
	return client.getStatusCodeReply();
    }

    public String mset(String... keysvalues) {
	checkIsInMulti();
	client.mset(keysvalues);
	return client.getStatusCodeReply();
    }

    public int msetnx(String... keysvalues) {
	checkIsInMulti();
	client.msetnx(keysvalues);
	return client.getIntegerReply();
    }

    public int decrBy(String key, int integer) {
	checkIsInMulti();
	client.decrBy(key, integer);
	return client.getIntegerReply();
    }

    public int decr(String key) {
	checkIsInMulti();
	client.decr(key);
	return client.getIntegerReply();
    }

    public int incrBy(String key, int integer) {
	checkIsInMulti();
	client.incrBy(key, integer);
	return client.getIntegerReply();
    }

    public int incr(String key) {
	checkIsInMulti();
	client.incr(key);
	return client.getIntegerReply();
    }

    public int append(String key, String value) {
	checkIsInMulti();
	client.append(key, value);
	return client.getIntegerReply();
    }

    public String substr(String key, int start, int end) {
	checkIsInMulti();
	client.substr(key, start, end);
	return client.getBulkReply();
    }

    public int hset(String key, String field, String value) {
	checkIsInMulti();
	client.hset(key, field, value);
	return client.getIntegerReply();
    }

    public String hget(String key, String field) {
	checkIsInMulti();
	client.hget(key, field);
	return client.getBulkReply();
    }

    public int hsetnx(String key, String field, String value) {
	checkIsInMulti();
	client.hsetnx(key, field, value);
	return client.getIntegerReply();
    }

    public String hmset(String key, Map<String, String> hash) {
	checkIsInMulti();
	client.hmset(key, hash);
	return client.getStatusCodeReply();
    }

    public List<String> hmget(String key, String... fields) {
	checkIsInMulti();
	client.hmget(key, fields);
	return client.getMultiBulkReply();
    }

    public int hincrBy(String key, String field, int value) {
	checkIsInMulti();
	client.hincrBy(key, field, value);
	return client.getIntegerReply();
    }

    public int hexists(String key, String field) {
	checkIsInMulti();
	client.hexists(key, field);
	return client.getIntegerReply();
    }

    public int hdel(String key, String field) {
	checkIsInMulti();
	client.hdel(key, field);
	return client.getIntegerReply();
    }

    public int hlen(String key) {
	checkIsInMulti();
	client.hlen(key);
	return client.getIntegerReply();
    }

    public List<String> hkeys(String key) {
	checkIsInMulti();
	client.hkeys(key);
	return client.getMultiBulkReply();
    }

    public List<String> hvals(String key) {
	checkIsInMulti();
	client.hvals(key);
	return client.getMultiBulkReply();
    }

    public Map<String, String> hgetAll(String key) {
	checkIsInMulti();
	client.hgetAll(key);
	List<String> flatHash = client.getMultiBulkReply();
	Map<String, String> hash = new HashMap<String, String>();
	Iterator<String> iterator = flatHash.iterator();
	while (iterator.hasNext()) {
	    hash.put(iterator.next(), iterator.next());
	}

	return hash;
    }

    public int rpush(String key, String string) {
	checkIsInMulti();
	client.rpush(key, string);
	return client.getIntegerReply();
    }

    public int lpush(String key, String string) {
	checkIsInMulti();
	client.lpush(key, string);
	return client.getIntegerReply();
    }

    public int llen(String key) {
	checkIsInMulti();
	client.llen(key);
	return client.getIntegerReply();
    }

    public List<String> lrange(String key, int start, int end) {
	checkIsInMulti();
	client.lrange(key, start, end);
	return client.getMultiBulkReply();
    }

    public String ltrim(String key, int start, int end) {
	checkIsInMulti();
	client.ltrim(key, start, end);
	return client.getStatusCodeReply();
    }

    public String lindex(String key, int index) {
	checkIsInMulti();
	client.lindex(key, index);
	return client.getBulkReply();
    }

    public String lset(String key, int index, String value) {
	checkIsInMulti();
	client.lset(key, index, value);
	return client.getStatusCodeReply();
    }

    public int lrem(String key, int count, String value) {
	checkIsInMulti();
	client.lrem(key, count, value);
	return client.getIntegerReply();
    }

    public String lpop(String key) {
	checkIsInMulti();
	client.lpop(key);
	return client.getBulkReply();
    }

    public String rpop(String key) {
	checkIsInMulti();
	client.rpop(key);
	return client.getBulkReply();
    }

    public String rpoplpush(String srckey, String dstkey) {
	checkIsInMulti();
	client.rpoplpush(srckey, dstkey);
	return client.getBulkReply();
    }

    public int sadd(String key, String member) {
	checkIsInMulti();
	client.sadd(key, member);
	return client.getIntegerReply();
    }

    public Set<String> smembers(String key) {
	checkIsInMulti();
	client.smembers(key);
	List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    public int srem(String key, String member) {
	checkIsInMulti();
	client.srem(key, member);
	return client.getIntegerReply();
    }

    public String spop(String key) {
	checkIsInMulti();
	client.spop(key);
	return client.getBulkReply();
    }

    public int smove(String srckey, String dstkey, String member) {
	checkIsInMulti();
	client.smove(srckey, dstkey, member);
	return client.getIntegerReply();
    }

    public int scard(String key) {
	checkIsInMulti();
	client.scard(key);
	return client.getIntegerReply();
    }

    public int sismember(String key, String member) {
	checkIsInMulti();
	client.sismember(key, member);
	return client.getIntegerReply();
    }

    public Set<String> sinter(String... keys) {
	checkIsInMulti();
	client.sinter(keys);
	List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    public int sinterstore(String dstkey, String... keys) {
	checkIsInMulti();
	client.sinterstore(dstkey, keys);
	return client.getIntegerReply();
    }

    public Set<String> sunion(String... keys) {
	checkIsInMulti();
	client.sunion(keys);
	List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    public int sunionstore(String dstkey, String... keys) {
	checkIsInMulti();
	client.sunionstore(dstkey, keys);
	return client.getIntegerReply();
    }

    public Set<String> sdiff(String... keys) {
	checkIsInMulti();
	client.sdiff(keys);
	List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    public int sdiffstore(String dstkey, String... keys) {
	checkIsInMulti();
	client.sdiffstore(dstkey, keys);
	return client.getIntegerReply();
    }

    public String srandmember(String key) {
	checkIsInMulti();
	client.srandmember(key);
	return client.getBulkReply();
    }

    public int zadd(String key, double score, String member) {
	checkIsInMulti();
	client.zadd(key, score, member);
	return client.getIntegerReply();
    }

    public Set<String> zrange(String key, int start, int end) {
	checkIsInMulti();
	client.zrange(key, start, end);
	List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    public int zrem(String key, String member) {
	checkIsInMulti();
	client.zrem(key, member);
	return client.getIntegerReply();
    }

    public double zincrby(String key, double score, String member) {
	checkIsInMulti();
	client.zincrby(key, score, member);
	String newscore = client.getBulkReply();
	return Double.valueOf(newscore);
    }

    public int zrank(String key, String member) {
	checkIsInMulti();
	client.zrank(key, member);
	return client.getIntegerReply();
    }

    public int zrevrank(String key, String member) {
	checkIsInMulti();
	client.zrevrank(key, member);
	return client.getIntegerReply();
    }

    public Set<String> zrevrange(String key, int start, int end) {
	checkIsInMulti();
	client.zrevrange(key, start, end);
	List<String> members = client.getMultiBulkReply();
	return new LinkedHashSet<String>(members);
    }

    public Set<Tuple> zrangeWithScores(String key, int start, int end) {
	checkIsInMulti();
	client.zrangeWithScores(key, start, end);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<Tuple> zrevrangeWithScores(String key, int start, int end) {
	checkIsInMulti();
	client.zrevrangeWithScores(key, start, end);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public int zcard(String key) {
	checkIsInMulti();
	client.zcard(key);
	return client.getIntegerReply();
    }

    public double zscore(String key, String member) {
	checkIsInMulti();
	client.zscore(key, member);
	String score = client.getBulkReply();
	return Double.valueOf(score);
    }

    public Transaction multi() {
	client.multi();
	client.getStatusCodeReply();
	return new Transaction(client);
    }

    public List<Object> multi(TransactionBlock jedisTransaction) {
	List<Object> results = null;
	try {
	    jedisTransaction.setClient(client);
	    multi();
	    jedisTransaction.execute();
	    results = jedisTransaction.exec();
	} catch (Exception ex) {
	    client.discard();
	}
	return results;
    }

    private void checkIsInMulti() {
	if (client.isInMulti()) {
	    throw new JedisException(
		    "Cannot use Jedis when in Multi. Please use JedisTransaction instead.");
	}
    }

    public void connect() throws UnknownHostException, IOException {
	client.connect();
    }

    public void disconnect() throws IOException {
	client.disconnect();
    }

    public String watch(String key) {
	client.watch(key);
	return client.getStatusCodeReply();
    }

    public String unwatch() {
	client.unwatch();
	return client.getStatusCodeReply();
    }

    public List<String> sort(String key) {
	checkIsInMulti();
	client.sort(key);
	return client.getMultiBulkReply();
    }

    public List<String> sort(String key, SortingParams sortingParameters) {
	checkIsInMulti();
	client.sort(key, sortingParameters);
	return client.getMultiBulkReply();
    }

    public List<String> blpop(int timeout, String... keys) {
	checkIsInMulti();
	List<String> args = new ArrayList<String>();
	for (String arg : keys) {
	    args.add(arg);
	}
	args.add(String.valueOf(timeout));

	client.blpop(args.toArray(new String[args.size()]));
	client.setTimeoutInfinite();
	List<String> multiBulkReply = client.getMultiBulkReply();
	client.rollbackTimeout();
	return multiBulkReply;
    }

    public int sort(String key, SortingParams sortingParameters, String dstkey) {
	checkIsInMulti();
	client.sort(key, sortingParameters, dstkey);
	return client.getIntegerReply();
    }

    public int sort(String key, String dstkey) {
	checkIsInMulti();
	client.sort(key, dstkey);
	return client.getIntegerReply();
    }

    public List<String> brpop(int timeout, String... keys) {
	checkIsInMulti();
	List<String> args = new ArrayList<String>();
	for (String arg : keys) {
	    args.add(arg);
	}
	args.add(String.valueOf(timeout));

	client.brpop(args.toArray(new String[args.size()]));
	client.setTimeoutInfinite();
	List<String> multiBulkReply = client.getMultiBulkReply();
	client.rollbackTimeout();

	return multiBulkReply;
    }

    public String auth(String password) {
	checkIsInMulti();
	client.auth(password);
	return client.getStatusCodeReply();
    }

    public List<Object> pipelined(JedisPipeline jedisPipeline) {
	jedisPipeline.setClient(client);
	jedisPipeline.execute();
	return client.getAll();
    }

    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
	client.setTimeoutInfinite();
	jedisPubSub.proceed(client, channels);
	client.rollbackTimeout();
    }

    public int publish(String channel, String message) {
	client.publish(channel, message);
	return client.getIntegerReply();
    }

    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
	client.setTimeoutInfinite();
	jedisPubSub.proceedWithPatterns(client, patterns);
	client.rollbackTimeout();
    }

    public int zcount(String key, double min, double max) {
	checkIsInMulti();
	client.zcount(key, min, max);
	return client.getIntegerReply();
    }

    public Set<String> zrangeByScore(String key, double min, double max) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrangeByScore(String key, double min, double max,
	    int offset, int count) {
	checkIsInMulti();
	client.zrangeByScore(key, min, max, offset, count);
	return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
	checkIsInMulti();
	client.zrangeByScoreWithScores(key, min, max);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min,
	    double max, int offset, int count) {
	checkIsInMulti();
	client.zrangeByScoreWithScores(key, min, max, offset, count);
	Set<Tuple> set = getTupledSet();
	return set;
    }

    private Set<Tuple> getTupledSet() {
	checkIsInMulti();
	List<String> membersWithScores = client.getMultiBulkReply();
	Set<Tuple> set = new LinkedHashSet<Tuple>();
	Iterator<String> iterator = membersWithScores.iterator();
	while (iterator.hasNext()) {
	    set
		    .add(new Tuple(iterator.next(), Double.valueOf(iterator
			    .next())));
	}
	return set;
    }

    public int zremrangeByRank(String key, int start, int end) {
	checkIsInMulti();
	client.zremrangeByRank(key, start, end);
	return client.getIntegerReply();
    }

    public int zremrangeByScore(String key, double start, double end) {
	checkIsInMulti();
	client.zremrangeByScore(key, start, end);
	return client.getIntegerReply();
    }

    public int zunionstore(String dstkey, String... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, sets);
	return client.getIntegerReply();
    }

    public int zunionstore(String dstkey, ZParams params, String... sets) {
	checkIsInMulti();
	client.zunionstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    public int zinterstore(String dstkey, String... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, sets);
	return client.getIntegerReply();
    }

    public int zinterstore(String dstkey, ZParams params, String... sets) {
	checkIsInMulti();
	client.zinterstore(dstkey, params, sets);
	return client.getIntegerReply();
    }

    public String save() {
	client.save();
	return client.getStatusCodeReply();
    }

    public String bgsave() {
	client.bgsave();
	return client.getStatusCodeReply();
    }

    public String bgrewriteaof() {
	client.bgrewriteaof();
	return client.getStatusCodeReply();
    }

    public int lastsave() {
	client.lastsave();
	return client.getIntegerReply();
    }

    public String shutdown() {
	client.shutdown();
	String status = null;
	try {
	    status = client.getStatusCodeReply();
	} catch (JedisException ex) {
	    status = null;
	}
	return status;
    }

    public String info() {
	client.info();
	return client.getBulkReply();
    }

    public void monitor(JedisMonitor jedisMonitor) {
	client.monitor();
	jedisMonitor.proceed(client);
    }

    public String slaveof(String host, int port) {
	client.slaveof(host, port);
	return client.getStatusCodeReply();
    }

    public String slaveofNoOne() {
	client.slaveofNoOne();
	return client.getStatusCodeReply();
    }

    public List<String> configGet(String pattern) {
	client.configGet(pattern);
	return client.getMultiBulkReply();
    }

    public String configSet(String parameter, String value) {
	client.configSet(parameter, value);
	return client.getStatusCodeReply();
    }

    public boolean isConnected() {
	return client.isConnected();
    }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
