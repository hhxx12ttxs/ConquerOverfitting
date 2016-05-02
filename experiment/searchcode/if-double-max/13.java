<<<<<<< HEAD
package redis.clients.jedis;

import static redis.clients.jedis.Protocol.toByteArray;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.exceptions.JedisDataException;

public class Pipeline extends Queable {
	
    private MultiResponseBuilder currentMulti;
    
    private class MultiResponseBuilder extends Builder<List<Object>>{
    	private List<Response<?>> responses = new ArrayList<Response<?>>();
		
		@Override
		public List<Object> build(Object data) {
			@SuppressWarnings("unchecked")
			List<Object> list = (List<Object>)data;
			List<Object> values = new ArrayList<Object>();
			
			if(list.size() != responses.size()){
				throw new JedisDataException("Expected data size " + responses.size() + " but was " + list.size());
			}
			
			for(int i=0;i<list.size();i++){
				Response<?> response = responses.get(i);
				response.set(list.get(i));
				values.add(response.get());
			}
			return values;
		}

		public void addResponse(Response<?> response){
			responses.add(response);
		}
    }

    @Override
    protected <T> Response<T> getResponse(Builder<T> builder) {
    	if(currentMulti != null){
    		super.getResponse(BuilderFactory.STRING); //Expected QUEUED
    		
    		Response<T> lr = new Response<T>(builder);
    		currentMulti.addResponse(lr);
    		return lr;
    	}
    	else{
    		return super.getResponse(builder);
    	}
    }
	
    private Client client;
    
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Syncronize pipeline by reading all responses. This operation close the
     * pipeline. In order to get return values from pipelined commands, capture
     * the different Response<?> of the commands you execute.
     */
    public void sync() {
        List<Object> unformatted = client.getAll();
        for (Object o : unformatted) {
            generateResponse(o);
        }
    }

    /**
     * Syncronize pipeline by reading all responses. This operation close the
     * pipeline. Whenever possible try to avoid using this version and use
     * Pipeline.sync() as it won't go through all the responses and generate the
     * right response type (usually it is a waste of time).
     * 
     * @return A list of all the responses in the order you executed them.
     * @see sync
     */
    public List<Object> syncAndReturnAll() {
        List<Object> unformatted = client.getAll();
        List<Object> formatted = new ArrayList<Object>();
        
        for (Object o : unformatted) {
            try {
            	formatted.add(generateResponse(o).get());
            } catch (JedisDataException e) {
                formatted.add(e);
            }
        }
        return formatted;
    }

    public Response<Long> append(String key, String value) {
        client.append(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> append(byte[] key, byte[] value) {
        client.append(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> blpop(String... args) {
        client.blpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> blpop(byte[]... args) {
        client.blpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> brpop(String... args) {
        client.brpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> brpop(byte[]... args) {
        client.brpop(args);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> decr(String key) {
        client.decr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decr(byte[] key) {
        client.decr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decrBy(String key, long integer) {
        client.decrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> decrBy(byte[] key, long integer) {
        client.decrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(String... keys) {
        client.del(keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> del(byte[]... keys) {
        client.del(keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> echo(String string) {
        client.echo(string);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> echo(byte[] string) {
        client.echo(string);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Boolean> exists(String key) {
        client.exists(key);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> exists(byte[] key) {
        client.exists(key);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Long> expire(String key, int seconds) {
        client.expire(key, seconds);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expire(byte[] key, int seconds) {
        client.expire(key, seconds);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expireAt(String key, long unixTime) {
        client.expireAt(key, unixTime);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> expireAt(byte[] key, long unixTime) {
        client.expireAt(key, unixTime);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> get(String key) {
        client.get(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> get(byte[] key) {
        client.get(key);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Boolean> getbit(String key, long offset) {
        client.getbit(key, offset);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> getrange(String key, long startOffset,
            long endOffset) {
        client.getrange(key, startOffset, endOffset);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> getSet(String key, String value) {
        client.getSet(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<byte[]> getSet(byte[] key, byte[] value) {
        client.getSet(key, value);
        return getResponse(BuilderFactory.BYTE_ARRAY);
    }

    public Response<Long> hdel(String key, String field) {
        client.hdel(key, field);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hdel(byte[] key, byte[] field) {
        client.hdel(key, field);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> hexists(String key, String field) {
        client.hexists(key, field);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> hexists(byte[] key, byte[] field) {
        client.hexists(key, field);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> hget(String key, String field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> hget(byte[] key, byte[] field) {
        client.hget(key, field);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Map<String, String>> hgetAll(String key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.STRING_MAP);
    }

    public Response<Map<String, String>> hgetAll(byte[] key) {
        client.hgetAll(key);
        return getResponse(BuilderFactory.STRING_MAP);
    }

    public Response<Long> hincrBy(String key, String field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hincrBy(byte[] key, byte[] field, long value) {
        client.hincrBy(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> hkeys(String key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> hkeys(byte[] key) {
        client.hkeys(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> hlen(String key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hlen(byte[] key) {
        client.hlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hmget(String key, String... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> hmget(byte[] key, byte[]... fields) {
        client.hmget(key, fields);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<String> hmset(String key, Map<String, String> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> hmset(byte[] key, Map<byte[], byte[]> hash) {
        client.hmset(key, hash);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> hset(String key, String field, String value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hset(byte[] key, byte[] field, byte[] value) {
        client.hset(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(String key, String field, String value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> hsetnx(byte[] key, byte[] field, byte[] value) {
        client.hsetnx(key, field, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> hvals(String key) {
        client.hvals(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> hvals(byte[] key) {
        client.hvals(key);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> incr(String key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incr(byte[] key) {
        client.incr(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(String key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> incrBy(byte[] key, long integer) {
        client.incrBy(key, integer);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> keys(String pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> keys(byte[] pattern) {
        client.keys(pattern);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<String> lindex(String key, int index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> lindex(byte[] key, int index) {
        client.lindex(key, index);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> linsert(String key, LIST_POSITION where,
            String pivot, String value) {
        client.linsert(key, where, pivot, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> linsert(byte[] key, LIST_POSITION where,
            byte[] pivot, byte[] value) {
        client.linsert(key, where, pivot, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> llen(String key) {
        client.llen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> llen(byte[] key) {
        client.llen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lpop(String key) {
        client.lpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> lpop(byte[] key) {
        client.lpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> lpush(String key, String string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpush(byte[] key, byte[] string) {
        client.lpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(String key, String string) {
        client.lpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lpushx(byte[] key, byte[] bytes) {
        client.lpushx(key, bytes);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> lrange(String key, long start, long end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> lrange(byte[] key, long start, long end) {
        client.lrange(key, start, end);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> lrem(String key, long count, String value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> lrem(byte[] key, long count, byte[] value) {
        client.lrem(key, count, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> lset(String key, long index, String value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> lset(byte[] key, long index, byte[] value) {
        client.lset(key, index, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(String key, long start, long end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> ltrim(byte[] key, long start, long end) {
        client.ltrim(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<List<String>> mget(String... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> mget(byte[]... keys) {
        client.mget(keys);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<Long> move(String key, int dbIndex) {
        client.move(key, dbIndex);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> move(byte[] key, int dbIndex) {
        client.move(key, dbIndex);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> mset(String... keysvalues) {
        client.mset(keysvalues);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> mset(byte[]... keysvalues) {
        client.mset(keysvalues);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> msetnx(String... keysvalues) {
        client.msetnx(keysvalues);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> msetnx(byte[]... keysvalues) {
        client.msetnx(keysvalues);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> persist(String key) {
        client.persist(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> persist(byte[] key) {
        client.persist(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> rename(String oldkey, String newkey) {
        client.rename(oldkey, newkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rename(byte[] oldkey, byte[] newkey) {
        client.rename(oldkey, newkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> renamenx(String oldkey, String newkey) {
        client.renamenx(oldkey, newkey);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> renamenx(byte[] oldkey, byte[] newkey) {
        client.renamenx(oldkey, newkey);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> rpop(String key) {
        client.rpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rpop(byte[] key) {
        client.rpop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rpoplpush(String srckey, String dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> rpoplpush(byte[] srckey, byte[] dstkey) {
        client.rpoplpush(srckey, dstkey);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> rpush(String key, String string) {
        client.rpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpush(byte[] key, byte[] string) {
        client.rpush(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpushx(String key, String string) {
        client.rpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> rpushx(byte[] key, byte[] string) {
        client.rpushx(key, string);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sadd(String key, String member) {
        client.sadd(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sadd(byte[] key, byte[] member) {
        client.sadd(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> scard(String key) {
        client.scard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> scard(byte[] key) {
        client.scard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> sdiff(String... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> sdiff(byte[]... keys) {
        client.sdiff(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sdiffstore(String dstkey, String... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sdiffstore(byte[] dstkey, byte[]... keys) {
        client.sdiffstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> set(String key, String value) {
        client.set(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> set(byte[] key, byte[] value) {
        client.set(key, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Boolean> setbit(String key, long offset, boolean value) {
        client.setbit(key, offset, value);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<String> setex(String key, int seconds, String value) {
        client.setex(key, seconds, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> setex(byte[] key, int seconds, byte[] value) {
        client.setex(key, seconds, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> setnx(String key, String value) {
        client.setnx(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> setnx(byte[] key, byte[] value) {
        client.setnx(key, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> setrange(String key, long offset, String value) {
        client.setrange(key, offset, value);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> sinter(String... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> sinter(byte[]... keys) {
        client.sinter(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sinterstore(String dstkey, String... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sinterstore(byte[] dstkey, byte[]... keys) {
        client.sinterstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Boolean> sismember(String key, String member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Boolean> sismember(byte[] key, byte[] member) {
        client.sismember(key, member);
        return getResponse(BuilderFactory.BOOLEAN);
    }

    public Response<Set<String>> smembers(String key) {
        client.smembers(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> smembers(byte[] key) {
        client.smembers(key);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> smove(String srckey, String dstkey, String member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> smove(byte[] srckey, byte[] dstkey, byte[] member) {
        client.smove(srckey, dstkey, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sort(String key) {
        client.sort(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sort(byte[] key) {
        client.sort(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<List<String>> sort(String key,
            SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key,
            SortingParams sortingParameters) {
        client.sort(key, sortingParameters);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(String key,
            SortingParams sortingParameters, String dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key,
            SortingParams sortingParameters, byte[] dstkey) {
        client.sort(key, sortingParameters, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(String key, String dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<List<String>> sort(byte[] key, byte[] dstkey) {
        client.sort(key, dstkey);
        return getResponse(BuilderFactory.STRING_LIST);
    }

    public Response<String> spop(String key) {
        client.spop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> spop(byte[] key) {
        client.spop(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> srandmember(String key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> srandmember(byte[] key) {
        client.srandmember(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> srem(String key, String member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> srem(byte[] key, byte[] member) {
        client.srem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(String key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> strlen(byte[] key) {
        client.strlen(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> substr(String key, int start, int end) {
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> substr(byte[] key, int start, int end) {
        client.substr(key, start, end);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Set<String>> sunion(String... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Set<String>> sunion(byte[]... keys) {
        client.sunion(keys);
        return getResponse(BuilderFactory.STRING_SET);
    }

    public Response<Long> sunionstore(String dstkey, String... keys) {
        client.sunionstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> sunionstore(byte[] dstkey, byte[]... keys) {
        client.sunionstore(dstkey, keys);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> ttl(String key) {
        client.ttl(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> ttl(byte[] key) {
        client.ttl(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> type(String key) {
        client.type(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> type(byte[] key) {
        client.type(key);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> watch(String... keys) {
        client.watch(keys);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> watch(byte[]... keys) {
        client.watch(keys);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> zadd(String key, double score, String member) {
        client.zadd(key, score, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zadd(byte[] key, double score, byte[] member) {
        client.zadd(key, score, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcard(String key) {
        client.zcard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcard(byte[] key) {
        client.zcard(key);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcount(String key, double min, double max) {
        client.zcount(key, min, max);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zcount(byte[] key, double min, double max) {
        client.zcount(key, toByteArray(min), toByteArray(max));
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zincrby(String key, double score, String member) {
        client.zincrby(key, score, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Double> zincrby(byte[] key, double score, byte[] member) {
        client.zincrby(key, score, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zinterstore(String dstkey, String... sets) {
        client.zinterstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(byte[] dstkey, byte[]... sets) {
        client.zinterstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(String dstkey, ZParams params,
            String... sets) {
        client.zinterstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zinterstore(byte[] dstkey, ZParams params,
            byte[]... sets) {
        client.zinterstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> zrange(String key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrange(byte[] key, int start, int end) {
        client.zrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(String key, double min,
            double max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, double min,
            double max) {
        return zrangeByScore(key, toByteArray(min), toByteArray(max));
    }
    
    public Response<Set<String>> zrangeByScore(String key, String min,
            String max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, byte[] min,
            byte[] max) {
        client.zrangeByScore(key, min, max);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(String key, double min,
            double max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrangeByScore(byte[] key, double min,
            double max, int offset, int count) {
        return zrangeByScore(key, toByteArray(min), toByteArray(max), offset, count);
    }
    
    public Response<Set<String>> zrangeByScore(byte[] key, byte[] min,
    		byte[] max, int offset, int count) {
        client.zrangeByScore(key, min, max, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
            double max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max) {
        return zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max));
    }
    
    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min,
    		byte[] max) {
        client.zrangeByScoreWithScores(key, min, max);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(String key, double min,
            double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, double min,
            double max, int offset, int count) {
        client.zrangeByScoreWithScores(key, toByteArray(min), toByteArray(max), offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }
    
    public Response<Set<Tuple>> zrangeByScoreWithScores(byte[] key, byte[] min,
    		byte[] max, int offset, int count) {
        client.zrangeByScoreWithScores(key, min, max, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, double max,
            double min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(byte[] key, double max,
            double min) {
        client.zrevrangeByScore(key, toByteArray(max), toByteArray(min));
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, String max,
            String min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(byte[] key, byte[] max,
            byte[] min) {
        client.zrevrangeByScore(key, max, min);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(String key, double max,
            double min, int offset, int count) {
        client.zrevrangeByScore(key, max, min, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrangeByScore(byte[] key, double max,
            double min, int offset, int count) {
        client.zrevrangeByScore(key, toByteArray(max), toByteArray(min), offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }
    
    public Response<Set<String>> zrevrangeByScore(byte[] key, byte[] max,
    		byte[] min, int offset, int count) {
        client.zrevrangeByScore(key, max, min, offset, count);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
            double max, double min) {
        client.zrevrangeByScoreWithScores(key, max, min);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
            double max, double min) {
        client.zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min));
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }
    
    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
    		byte[] max, byte[] min) {
        client.zrevrangeByScoreWithScores(key, max, min);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(String key,
            double max, double min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, max, min, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
            double max, double min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, toByteArray(max), toByteArray(min), offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }
    
    public Response<Set<Tuple>> zrevrangeByScoreWithScores(byte[] key,
    		byte[] max, byte[] min, int offset, int count) {
        client.zrevrangeByScoreWithScores(key, max, min, offset, count);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeWithScores(String key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrangeWithScores(byte[] key, int start, int end) {
        client.zrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Long> zrank(String key, String member) {
        client.zrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrank(byte[] key, byte[] member) {
        client.zrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrem(String key, String member) {
        client.zrem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrem(byte[] key, byte[] member) {
        client.zrem(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByRank(String key, int start, int end) {
        client.zremrangeByRank(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByRank(byte[] key, int start, int end) {
        client.zremrangeByRank(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(String key, double start, double end) {
        client.zremrangeByScore(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zremrangeByScore(byte[] key, double start, double end) {
        client.zremrangeByScore(key, toByteArray(start), toByteArray(end));
        return getResponse(BuilderFactory.LONG);
    }
    
    public Response<Long> zremrangeByScore(byte[] key, byte[] start, byte[] end) {
        client.zremrangeByScore(key, start, end);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Set<String>> zrevrange(String key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<String>> zrevrange(byte[] key, int start, int end) {
        client.zrevrange(key, start, end);
        return getResponse(BuilderFactory.STRING_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(String key, int start,
            int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Set<Tuple>> zrevrangeWithScores(byte[] key, int start,
            int end) {
        client.zrevrangeWithScores(key, start, end);
        return getResponse(BuilderFactory.TUPLE_ZSET);
    }

    public Response<Long> zrevrank(String key, String member) {
        client.zrevrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zrevrank(byte[] key, byte[] member) {
        client.zrevrank(key, member);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Double> zscore(String key, String member) {
        client.zscore(key, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Double> zscore(byte[] key, byte[] member) {
        client.zscore(key, member);
        return getResponse(BuilderFactory.DOUBLE);
    }

    public Response<Long> zunionstore(String dstkey, String... sets) {
        client.zunionstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(byte[] dstkey, byte[]... sets) {
        client.zunionstore(dstkey, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(String dstkey, ZParams params,
            String... sets) {
        client.zunionstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> zunionstore(byte[] dstkey, ZParams params,
            byte[]... sets) {
        client.zunionstore(dstkey, params, sets);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> bgrewriteaof() {
        client.bgrewriteaof();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> bgsave() {
        client.bgsave();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configGet(String pattern) {
        client.configGet(pattern);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configSet(String parameter, String value) {
        client.configSet(parameter, value);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> brpoplpush(String source, String destination,
            int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> brpoplpush(byte[] source, byte[] destination,
            int timeout) {
        client.brpoplpush(source, destination, timeout);
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> configResetStat() {
        client.configResetStat();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<String> save() {
        client.save();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<Long> lastsave() {
        client.lastsave();
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> discard() {
        client.discard();
        return getResponse(BuilderFactory.STRING);
    }

    public Response<List<Object>> exec() {
        client.exec();
        Response<List<Object>> response = super.getResponse(currentMulti);
        currentMulti = null;
        return response;
    }

    public void multi() {
        client.multi();
        getResponse(BuilderFactory.STRING); //Expecting OK
        currentMulti = new MultiResponseBuilder();
    }

    public Response<Long> publish(String channel, String message) {
        client.publish(channel, message);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<Long> publish(byte[] channel, byte[] message) {
        client.publish(channel, message);
        return getResponse(BuilderFactory.LONG);
    }

    public Response<String> flushDB() {
        client.flushDB();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> flushAll() {
        client.flushAll();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> info() {
        client.info();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<Long> dbSize() {
        client.dbSize();
        return getResponse(BuilderFactory.LONG);
    }
    
    public Response<String> shutdown() {
        client.shutdown();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> ping() {
        client.ping();
        return getResponse(BuilderFactory.STRING);
    }
    
    public Response<String> randomKey() {
        client.randomKey();
        return getResponse(BuilderFactory.STRING);
    }   
    
    public Response<String> select(int index){
    	client.select(index);
    	return getResponse(BuilderFactory.STRING);
    }    
}
=======
package org.doube.bonej;

/**
 * SliceGeometry plugin for ImageJ
 * Copyright 2009 2010 Michael Doube 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.StackConverter;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.gui.Wand;
import ij3d.Content;
import ij3d.Image3DUniverse;

import java.awt.AWTEvent;
import java.awt.Checkbox;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.TextField;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import org.doube.geometry.Orienteer;
import org.doube.util.DialogModifier;
import org.doube.util.ImageCheck;
import org.doube.util.ThresholdGuesser;
import org.doube.util.UsageReporter;

import customnode.CustomPointMesh;

/**
 * <p>
 * Calculate 2D geometrical parameters
 * </p>
 * 
 * @author Michael Doube
 * 
 */

public class SliceGeometry implements PlugIn, DialogListener {
	private Calibration cal;
	private int al, startSlice, endSlice;
	private double vW, vH;// , min, max;
	/** Linear unit of measure */
	private String units;
	/** Do local thickness measurement in 3D */
	private boolean doThickness3D;
	/** Do local thickness measurement in 2D */
	private boolean doThickness2D;
	/** Show slice centroid */
	private boolean doCentroids;
	/** Show principal axes */
	private boolean doAxes;
	/** if true, show annotation in a new window */
	private boolean doCopy;
	/** If true, process the whole stack */
	private boolean doStack;
	/** Number of thresholded pixels in each slice */
	private double[] cslice;
	/** Cross-sectional area */
	private double[] cortArea;
	/** Mean of 3D local thickness in slice */
	private double[] meanCortThick3D;
	/** Maximum 3D local thickness in slice */
	private double[] maxCortThick3D;
	/** Standard deviation of 3D local thickness in slice */
	private double[] stdevCortThick3D;
	/** Mean of 2D local thickness in slice */
	private double[] meanCortThick2D;
	/** Maximum 2D local thickness in slice */
	private double[] maxCortThick2D;
	/** Standard deviation of 2D local thickness in slice */
	private double[] stdevCortThick2D;
	/** normal x distance from parallel axis summed over pixels */
	// private double[] Sx;
	/** normal y distance from parallel axis summed over pixels */
	// private double[] Sy;
	/** squared normal distances from parallel axis (Iz) */
	// private double[] Sxx;
	/** squared normal distances from parallel axis (Iz) */
	// private double[] Syy;
	// private double[] Sxy;
	// private double[] Myy;
	// private double[] Mxx;
	// private double[] Mxy;
	/** Angle of principal axes */
	private double[] theta;
	/**
	 * 2nd moment of area around minimum principal axis (shorter axis, larger I)
	 */
	private double[] Imax;
	/**
	 * 2nd moment of area around maximum principal axis (longer axis, smaller I)
	 */
	private double[] Imin;
	/** product moment of area, should be 0 if theta calculated perfectly */
	private double[] Ipm;
	/** length of major axis */
	private double[] R1;
	/** length of minor axis */
	private double[] R2;
	/** maximum distance from minimum principal axis (longer) */
	private double[] maxRadMin;
	/** maximum distance from maximum principal axis (shorter) */
	private double[] maxRadMax;
	/** Section modulus around minimum principal axis */
	private double[] Zmax;
	/** Section modulus around maximum principal axis */
	private double[] Zmin;
	/** Maximum diameter */
	private double[] feretMax;
	/** Angle of maximum diameter */
	private double[] feretAngle;
	/** Minimum diameter */
	private double[] feretMin;
	/** List of empty slices. If true, slice contains 0 pixels to analyse */
	private boolean[] emptySlices;
	/** List of slice centroids */
	private double[][] sliceCentroids;
	private double[] integratedDensity;
	private double[] meanDensity;
	private double m;
	private double c;
	private double[][] weightedCentroids;
	private boolean fieldUpdated = false;
	/** List of perimeter lengths */
	private double[] perimeter;
	/** List of maximal distances from centroid */
	// private double[] maxRadCentre;
	/** List of polar section moduli */
	private double[] Zpol;
	private boolean do3DAnnotation;
	private Orienteer orienteer;
	/** Flag to use anatomic orientation */
	private boolean doOriented;
	/** Second moment of area around primary axis */
	private double[] I1;
	/** Second moment of area around secondary axis */
	private double[] I2;
	// private double[] Ip;
	// private double[] r1;
	// private double[] r2;
	/** Chord length from principal axis */
	private double[] maxRad2;
	/** Chord length from secondary axis */
	private double[] maxRad1;
	/** Section modulus around primary axis */
	private double[] Z1;
	/** Section modulus around secondary axis */
	private double[] Z2;
	// private double[] Zp;
	private double[] principalDiameter;
	private double[] secondaryDiameter;
	/** Flag to clear the results table or concatenate */
	private boolean clearResults;

	public void run(String arg) {
		if (!ImageCheck.checkEnvironment())
			return;
		ImagePlus imp = IJ.getImage();
		if (null == imp) {
			IJ.noImage();
			return;
		}
		this.cal = imp.getCalibration();
		this.vW = cal.pixelWidth;
		this.vH = cal.pixelHeight;
		this.units = cal.getUnits();
		this.al = imp.getStackSize() + 1;

		String pixUnits;
		if (ImageCheck.huCalibrated(imp)) {
			pixUnits = "HU";
			fieldUpdated = true;
		} else
			pixUnits = "grey";

		double[] thresholds = ThresholdGuesser.setDefaultThreshold(imp);
		double min = thresholds[0];
		double max = thresholds[1];
		orienteer = Orienteer.getInstance();

		GenericDialog gd = new GenericDialog("Options");

		// guess bone from image title
		int boneID = BoneList.guessBone(imp);
		String[] bones = BoneList.get();
		gd.addChoice("Bone: ", bones, bones[boneID]);

		gd.addCheckbox("2D_Thickness", true);
		gd.addCheckbox("3D_Thickness", false);
		gd.addCheckbox("Draw_Axes", true);
		gd.addCheckbox("Draw_Centroids", true);
		gd.addCheckbox("Annotated_Copy_(2D)", true);
		gd.addCheckbox("3D_Annotation", false);
		gd.addCheckbox("Process_Stack", false);
		gd.addCheckbox("Clear_results", false);
		gd.addCheckbox("Use Orientation", (orienteer != null));
		// String[] analyses = { "Weighted", "Unweighted", "Both" };
		// gd.addChoice("Calculate: ", analyses, analyses[1]);
		gd.addCheckbox("HU_Calibrated", ImageCheck.huCalibrated(imp));
		gd.addNumericField("Bone_Min:", min, 1, 6, pixUnits + " ");
		gd.addNumericField("Bone_Max:", max, 1, 6, pixUnits + " ");
		gd
				.addMessage("Only pixels >= bone min\n"
						+ "and <= bone max are used.");
		gd.addMessage("Density calibration coefficients");
		gd.addNumericField("Slope", 0, 4, 6, "g.cm^-3 / " + pixUnits + " ");
		gd.addNumericField("Y_Intercept", 1.8, 4, 6, "g.cm^-3");
		gd.addHelp("http://bonej.org/slice");
		gd.addDialogListener(this);
		gd.showDialog();
		String bone = gd.getNextChoice();
		boneID = BoneList.guessBone(bone);
		this.doThickness2D = gd.getNextBoolean();
		this.doThickness3D = gd.getNextBoolean();
		this.doAxes = gd.getNextBoolean();
		this.doCentroids = gd.getNextBoolean();
		this.doCopy = gd.getNextBoolean();
		this.do3DAnnotation = gd.getNextBoolean();
		this.doStack = gd.getNextBoolean();
		this.clearResults = gd.getNextBoolean();
		this.doOriented = gd.getNextBoolean();
		if (this.doStack) {
			this.startSlice = 1;
			this.endSlice = imp.getImageStackSize();
		} else {
			this.startSlice = imp.getCurrentSlice();
			this.endSlice = imp.getCurrentSlice();
		}

		boolean isHUCalibrated = gd.getNextBoolean();
		min = gd.getNextNumber();
		max = gd.getNextNumber();
		if (isHUCalibrated) {
			min = cal.getRawValue(min);
			max = cal.getRawValue(max);
		}
		this.m = gd.getNextNumber();
		this.c = gd.getNextNumber();
		if (gd.wasCanceled())
			return;

		if (calculateCentroids(imp, min, max) == 0) {
			IJ.error("No pixels available to calculate.\n"
					+ "Please check the threshold and ROI.");
			return;
		}

		calculateMoments(imp, min, max);
		if (this.doThickness3D)
			calculateThickness3D(imp, min, max);
		if (this.doThickness2D)
			calculateThickness2D(imp, min, max);

		roiMeasurements(imp, min, max);

		// TODO locate centroids of multiple sections in a single plane

		ResultsTable rt = ResultsTable.getResultsTable();
		if (clearResults)
			rt.reset();

		String title = imp.getTitle();
		for (int s = this.startSlice; s <= this.endSlice; s++) {
			rt.incrementCounter();
			rt.addLabel(title);
			rt.addValue("Bone Code", boneID);
			rt.addValue("Slice", s);
			rt.addValue("CSA (" + units + "²)", this.cortArea[s]);
			rt.addValue("X cent. (" + units + ")", this.sliceCentroids[0][s]);
			rt.addValue("Y cent. (" + units + ")", this.sliceCentroids[1][s]);
			rt.addValue("Density", this.meanDensity[s]);
			rt.addValue("wX cent. (" + units + ")",
					this.weightedCentroids[0][s]);
			rt.addValue("wY cent. (" + units + ")",
					this.weightedCentroids[1][s]);
			rt.addValue("Theta (rad)", this.theta[s]);
			rt.addValue("R1 (" + units + ")", this.maxRadMax[s]);
			rt.addValue("R2 (" + units + ")", this.maxRadMin[s]);
			rt.addValue("Imin (" + units + "^4)", this.Imin[s]);
			rt.addValue("Imax (" + units + "^4)", this.Imax[s]);
			rt.addValue("Ipm (" + units + "^4)", this.Ipm[s]);
			rt.addValue("Zmax (" + units + "³)", this.Zmax[s]);
			rt.addValue("Zmin (" + units + "³)", this.Zmin[s]);
			rt.addValue("Zpol (" + units + "³)", this.Zpol[s]);
			rt.addValue("Feret Min (" + units + ")", this.feretMin[s]);
			rt.addValue("Feret Max (" + units + ")", this.feretMax[s]);
			rt.addValue("Feret Angle (rad)", this.feretAngle[s]);
			rt.addValue("Perimeter (" + units + ")", this.perimeter[s]);
			if (this.doThickness3D) {
				rt.addValue("Max Thick 3D (" + units + ")",
						this.maxCortThick3D[s]);
				rt.addValue("Mean Thick 3D (" + units + ")",
						this.meanCortThick3D[s]);
				rt.addValue("SD Thick 3D (" + units + ")",
						this.stdevCortThick3D[s]);
			}
			if (this.doThickness2D) {
				rt.addValue("Max Thick 2D (" + units + ")",
						this.maxCortThick2D[s]);
				rt.addValue("Mean Thick 2D (" + units + ")",
						this.meanCortThick2D[s]);
				rt.addValue("SD Thick 2D (" + units + ")",
						this.stdevCortThick2D[s]);
			}
			if (this.doOriented && orienteer != null) {
				String[] dirs = orienteer.getDirections(imp);
				rt.addValue(dirs[0] + " (rad)", orienteer.getOrientation(imp,
						dirs[0]));
				rt.addValue(dirs[2] + " (rad)", orienteer.getOrientation(imp,
						dirs[2]));
				rt.addValue("I" + dirs[0] + dirs[1] + "(" + units + "^4)",
						this.I1[s]);
				rt.addValue("I" + dirs[2] + dirs[3] + "(" + units + "^4)",
						this.I2[s]);
				rt.addValue("Z" + dirs[0] + dirs[1] + "(" + units + "³)",
						this.Z1[s]);
				rt.addValue("Z" + dirs[2] + dirs[3] + "(" + units + "³)",
						this.Z2[s]);
				rt.addValue("R" + dirs[0] + dirs[1] + "(" + units + ")",
						this.maxRad2[s]);
				rt.addValue("R" + dirs[2] + dirs[3] + "(" + units + ")",
						this.maxRad1[s]);
				rt.addValue("D" + dirs[0] + dirs[1] + "(" + units + ")",
						this.principalDiameter[s]);
				rt.addValue("D" + dirs[2] + dirs[3] + "(" + units + ")",
						this.secondaryDiameter[s]);
			}
		}
		rt.show("Results");

		if (this.doAxes || this.doCentroids) {
			if (!this.doCopy) {
				ImagePlus annImp = annotateImage(imp);
				imp.setStack(null, annImp.getImageStack());
			} else {
				annotateImage(imp).show();
			}
		}
		if (this.do3DAnnotation)
			show3DAxes(imp);
		UsageReporter.reportEvent(this).send();
		return;
	}

	/**
	 * Draw centroids and / or principal axes on a copy of the original image
	 * 
	 * @param imp
	 * @return ImagePlus with centroid and / or principal axes drawn
	 */
	private ImagePlus annotateImage(ImagePlus imp) {
		ImageStack stack = imp.getImageStack();
		int w = stack.getWidth();
		int h = stack.getHeight();
		ImageStack annStack = new ImageStack(w, h);
		for (int s = this.startSlice; s <= this.endSlice; s++) {
			ImageProcessor annIP = stack.getProcessor(s).duplicate();
			annIP.setColor(Color.white);
			double cX = this.sliceCentroids[0][s] / this.vW;
			double cY = this.sliceCentroids[1][s] / this.vH;

			if (this.doCentroids && !this.emptySlices[s]) {
				annIP.drawOval((int) Math.floor(cX - 4), (int) Math
						.floor(cY - 4), 8, 8);
			}

			if (this.doAxes && !this.emptySlices[s]) {
				double th = this.theta[s];
				double rMin = this.R1[s];
				double rMax = this.R2[s];
				double thPi = th + Math.PI / 2;

				int x1 = (int) Math.floor(cX - Math.cos(thPi) * 2 * rMin);
				int y1 = (int) Math.floor(cY - Math.sin(thPi) * 2 * rMin);
				int x2 = (int) Math.floor(cX + Math.cos(thPi) * 2 * rMin);
				int y2 = (int) Math.floor(cY + Math.sin(thPi) * 2 * rMin);
				annIP.drawLine(x1, y1, x2, y2);

				x1 = (int) Math.floor(cX - Math.cos(-th) * 2 * rMax);
				y1 = (int) Math.floor(cY + Math.sin(-th) * 2 * rMax);
				x2 = (int) Math.floor(cX + Math.cos(-th) * 2 * rMax);
				y2 = (int) Math.floor(cY - Math.sin(-th) * 2 * rMax);
				annIP.drawLine(x1, y1, x2, y2);
			}
			annStack.addSlice(stack.getSliceLabel(s), annIP);
		}
		ImagePlus ann = new ImagePlus("Annotated_" + imp.getTitle(), annStack);
		ann.setCalibration(imp.getCalibration());
		if (ann.getImageStackSize() == 1)
			ann.setProperty("Info", stack.getSliceLabel(this.startSlice));
		return ann;
	}

	/**
	 * Display principal axes on a 3D rendered version of the image
	 * 
	 * @param imp
	 *            Original image
	 */
	private void show3DAxes(ImagePlus imp) {
		Calibration cal = imp.getCalibration();
		// copy the data from inside the ROI and convert it to 8-bit
		Duplicator d = new Duplicator();
		ImagePlus roiImp = d.run(imp, 1, imp.getImageStackSize());

		// initialise and show the 3D universe
		Image3DUniverse univ = new Image3DUniverse();
		univ.show();

		double rX = 0;
		double rY = 0;
		if (imp.getRoi() != null) {
			Rectangle roi = imp.getRoi().getBounds();
			rX = roi.getX() * cal.pixelWidth;
			rY = roi.getY() * cal.pixelHeight;
		}

		// list of centroids
		List<Point3f> centroids = new ArrayList<Point3f>();
		// list of axes
		List<Point3f> axes = new ArrayList<Point3f>();
		for (int s = 1; s <= roiImp.getImageStackSize(); s++) {
			if (((Double) this.cortArea[s]).equals(Double.NaN))
				continue;

			final double cX = sliceCentroids[0][s] - rX;
			final double cY = sliceCentroids[1][s] - rY;
			final double cZ = (s - 0.5) * cal.pixelDepth;

			Point3f cent = new Point3f();
			cent.x = (float) cX;
			cent.y = (float) cY;
			cent.z = (float) cZ;
			centroids.add(cent);

			// add the axes to the list
			double th = this.theta[s];
			double rMin = this.R1[s] * cal.pixelWidth;
			double rMax = this.R2[s] * cal.pixelWidth;
			double thPi = th + Math.PI / 2;

			Point3f start1 = new Point3f();
			start1.x = (float) (cX - Math.cos(thPi) * 2 * rMin);
			start1.y = (float) (cY - Math.sin(thPi) * 2 * rMin);
			start1.z = (float) cZ;
			axes.add(start1);

			Point3f end1 = new Point3f();
			end1.x = (float) (cX + Math.cos(thPi) * 2 * rMin);
			end1.y = (float) (cY + Math.sin(thPi) * 2 * rMin);
			end1.z = (float) cZ;
			axes.add(end1);

			Point3f start2 = new Point3f();
			start2.x = (float) (cX - Math.cos(-th) * 2 * rMax);
			start2.y = (float) (cY + Math.sin(-th) * 2 * rMax);
			start2.z = (float) cZ;
			axes.add(start2);

			Point3f end2 = new Point3f();
			end2.x = (float) (cX + Math.cos(-th) * 2 * rMax);
			end2.y = (float) (cY - Math.sin(-th) * 2 * rMax);
			end2.z = (float) cZ;
			axes.add(end2);
		}
		// show the centroids
		CustomPointMesh mesh = new CustomPointMesh(centroids);
		mesh.setPointSize(5.0f);
		float red = 0.0f;
		float green = 0.5f;
		float blue = 1.0f;
		Color3f cColour = new Color3f(red, green, blue);
		mesh.setColor(cColour);
		try {
			univ.addCustomMesh(mesh, "Centroid").setLocked(true);
		} catch (NullPointerException npe) {
			IJ.log("3D Viewer was closed before rendering completed.");
			return;
		}

		// show the axes
		Color3f aColour = new Color3f(red, green, blue);
		try {
			univ.addLineMesh(axes, aColour, "Principal axes", false).setLocked(
					true);
		} catch (NullPointerException npe) {
			IJ.log("3D Viewer was closed before rendering completed.");
			return;
		}
		// show the stack
		try {
			new StackConverter(roiImp).convertToGray8();
			Content c = univ.addVoltex(roiImp);
			c.setLocked(true);
		} catch (NullPointerException npe) {
			IJ.log("3D Viewer was closed before rendering completed.");
			return;
		}
		return;
	}

	/**
	 * Calculate the centroid of each slice
	 * 
	 * @param imp
	 *            Input image
	 * @return double containing sum of pixel count
	 */
	private double calculateCentroids(ImagePlus imp, double min, double max) {
		ImageStack stack = imp.getImageStack();
		Rectangle r = stack.getRoi();
		// 2D centroids
		this.sliceCentroids = new double[2][this.al];
		// pixel counters
		double cstack = 0;
		this.emptySlices = new boolean[this.al];
		this.cslice = new double[this.al];
		this.cortArea = new double[this.al];
		this.integratedDensity = new double[this.al];
		this.meanDensity = new double[this.al];
		this.weightedCentroids = new double[2][this.al];
		final double pixelArea = this.vW * this.vH;
		final int roiXEnd = r.x + r.width;
		final int roiYEnd = r.y + r.height;
		for (int s = this.startSlice; s <= this.endSlice; s++) {
			IJ.showStatus("Calculating centroids...");
			IJ.showProgress(s - this.startSlice, this.endSlice);
			double sumX = 0;
			double sumY = 0;
			int count = 0;
			double sumD = 0;
			double wSumX = 0;
			double wSumY = 0;
			ImageProcessor ip = stack.getProcessor(s);
			for (int y = r.y; y < roiYEnd; y++) {
				for (int x = r.x; x < roiXEnd; x++) {
					final double pixel = (double) ip.get(x, y);
					if (pixel >= min && pixel <= max) {
						count++;
						sumX += x;
						sumY += y;
						final double wP = pixel * this.m + this.c;
						sumD += wP;
						wSumX += x * wP;
						wSumY += y * wP;
					}
				}
			}
			this.cslice[s] = count;
			this.cortArea[s] = count * pixelArea;
			if (count > 0) {
				this.sliceCentroids[0][s] = sumX * this.vW / count;
				this.sliceCentroids[1][s] = sumY * this.vH / count;
				this.integratedDensity[s] = sumD;
				this.meanDensity[s] = sumD / count;
				this.weightedCentroids[0][s] = wSumX * this.vW / sumD;
				this.weightedCentroids[1][s] = wSumY * this.vH / sumD;
				cstack += count;
				this.emptySlices[s] = false;
			} else {
				this.emptySlices[s] = true;
				this.cortArea[s] = Double.NaN;
				this.sliceCentroids[0][s] = Double.NaN;
				this.sliceCentroids[1][s] = Double.NaN;
				this.cslice[s] = Double.NaN;
			}
		}
		return cstack;
	}

	/**
	 * Calculate second moments of area, length and angle of principal axes
	 * 
	 * @param imp
	 */
	private void calculateMoments(ImagePlus imp, double min, double max) {
		final ImageStack stack = imp.getImageStack();
		final Rectangle r = stack.getRoi();
		// START OF Ix AND Iy CALCULATION
		// this.Sx = new double[this.al];
		// this.Sy = new double[this.al];
		// this.Sxx = new double[this.al];
		// this.Syy = new double[this.al];
		// this.Sxy = new double[this.al];
		// this.Myy = new double[this.al];
		// this.Mxx = new double[this.al];
		// this.Mxy = new double[this.al];
		this.theta = new double[this.al];
		for (int s = this.startSlice; s <= this.endSlice; s++) {
			IJ.showStatus("Calculating Ix and Iy...");
			IJ.showProgress(s, this.endSlice);
			double sxs = 0;
			double sys = 0;
			double sxxs = 0;
			double syys = 0;
			double sxys = 0;
			final int roiXEnd = r.x + r.width;
			final int roiYEnd = r.y + r.height;
			if (!this.emptySlices[s]) {
				ImageProcessor ip = stack.getProcessor(s);
				for (int y = r.y; y < roiYEnd; y++) {
					for (int x = r.x; x < roiXEnd; x++) {
						final double pixel = (double) ip.get(x, y);
						if (pixel >= min && pixel <= max) {
							final double xVw = x * vW;
							final double yVh = y * vH;
							sxs += xVw;
							sys += yVh;
							sxxs += xVw * xVw;
							syys += yVh * yVh;
							sxys += xVw * yVh;
						}
					}
				}
				// this.Sx[s] = sxs;
				// this.Sy[s] = sys;
				// this.Sxx[s] = sxxs;
				// this.Syy[s] = syys;
				// this.Sxy[s] = sxys;
				double Myys = sxxs - (sxs * sxs / this.cslice[s])
						+ this.cslice[s] * vW * vW / 12;
				// this.cslice[]/12 is for each pixel's own moment
				double Mxxs = syys - (sys * sys / this.cslice[s])
						+ this.cslice[s] * vH * vH / 12;
				double Mxys = sxys - (sxs * sys / this.cslice[s])
						+ this.cslice[s] * vH * vW / 12;
				if (Mxys == 0)
					this.theta[s] = 0;
				else {
					this.theta[s] = Math.atan((Mxxs - Myys + Math
							.sqrt((Mxxs - Myys) * (Mxxs - Myys) + 4 * Mxys
									* Mxys))
							/ (2 * Mxys));
				}
			} else {
				this.theta[s] = Double.NaN;
			}
		}
		// Get I and Z around the principal axes
		double[][] result = calculateAngleMoments(imp, min, max, this.theta);
		this.Imax = result[0];
		this.Imin = result[1];
		this.Ipm = result[2];
		this.R1 = result[3];
		this.R2 = result[4];
		this.maxRadMin = result[5];
		this.maxRadMax = result[6];
		this.Zmax = result[7];
		this.Zmin = result[8];
		this.Zpol = result[9];

		// optionally get I and Z around some user-defined axes
		if (doOriented && orienteer != null) {
			double angle = orienteer.getOrientation();
			double[] angles = new double[this.al];
			for (int i = 0; i < al; i++) {
				angles[i] = angle;
			}
			double[][] result2 = calculateAngleMoments(imp, min, max, angles);
			this.I1 = result2[0];
			this.I2 = result2[1];
			// this.Ip = result2[2];
			// this.r1 = result2[3];
			// this.r2 = result2[4];
			this.maxRad2 = result2[5];
			this.maxRad1 = result2[6];
			this.Z1 = result2[7];
			this.Z2 = result2[8];
			// this.Zp = result2[9];
		}
	}

	private double[][] calculateAngleMoments(ImagePlus imp, double min,
			double max, double[] angles) {
		final ImageStack stack = imp.getImageStack();
		final Rectangle r = stack.getRoi();
		// END OF Ix and Iy CALCULATION
		// START OF Imax AND Imin CALCULATION
		double[] I1 = new double[this.al];
		double[] I2 = new double[this.al];
		double[] Ip = new double[this.al];
		double[] r1 = new double[this.al];
		double[] r2 = new double[this.al];
		double[] maxRad2 = new double[this.al];
		double[] maxRad1 = new double[this.al];
		double[] maxRadC = new double[this.al];
		double[] Z1 = new double[this.al];
		double[] Z2 = new double[this.al];
		double[] Zp = new double[this.al];
		for (int s = this.startSlice; s <= this.endSlice; s++) {
			IJ.showStatus("Calculating Imin and Imax...");
			IJ.showProgress(s, this.endSlice);
			if (!this.emptySlices[s]) {
				ImageProcessor ip = stack.getProcessor(s);
				double sxs = 0;
				double sys = 0;
				double sxxs = 0;
				double syys = 0;
				double sxys = 0;
				double maxRadMinS = 0;
				double maxRadMaxS = 0;
				double maxRadCentreS = 0;
				final double cosTheta = Math.cos(angles[s]);
				final double sinTheta = Math.sin(angles[s]);
				final int roiYEnd = r.y + r.height;
				final int roiXEnd = r.x + r.width;
				final double xC = this.sliceCentroids[0][s];
				final double yC = this.sliceCentroids[1][s];
				final double cS = this.cslice[s];
				for (int y = r.y; y < roiYEnd; y++) {
					final double yYc = y * vH - yC;
					for (int x = r.x; x < roiXEnd; x++) {
						final double pixel = (double) ip.get(x, y);
						if (pixel >= min && pixel <= max) {
							final double xXc = x * vW - xC;
							final double xCosTheta = x * vW * cosTheta;
							final double yCosTheta = y * vH * cosTheta;
							final double xSinTheta = x * vW * sinTheta;
							final double ySinTheta = y * vH * sinTheta;
							sxs += xCosTheta + ySinTheta;
							sys += yCosTheta - xSinTheta;
							sxxs += (xCosTheta + ySinTheta)
									* (xCosTheta + ySinTheta);
							syys += (yCosTheta - xSinTheta)
									* (yCosTheta - xSinTheta);
							sxys += (yCosTheta - xSinTheta)
									* (xCosTheta + ySinTheta);
							maxRadMinS = Math.max(maxRadMinS, Math.abs(xXc
									* cosTheta + yYc * sinTheta));
							maxRadMaxS = Math.max(maxRadMaxS, Math.abs(yYc
									* cosTheta - xXc * sinTheta));
							maxRadCentreS = Math.max(maxRadCentreS, Math
									.sqrt(xXc * xXc + yYc * yYc));
						}
					}
				}
				// this.Sx[s] = sxs;
				// this.Sy[s] = sys;
				// this.Sxx[s] = sxxs;
				// this.Syy[s] = syys;
				// this.Sxy[s] = sxys;
				maxRad2[s] = maxRadMinS;
				maxRad1[s] = maxRadMaxS;
				maxRadC[s] = maxRadCentreS;
				final double pixelMoments = cS * vW * vH
						* (cosTheta * cosTheta + sinTheta * sinTheta) / 12;
				I1[s] = vW * vH * (sxxs - (sxs * sxs / cS) + pixelMoments);
				I2[s] = vW * vH * (syys - (sys * sys / cS) + pixelMoments);
				Ip[s] = sxys - (sys * sxs / cS) + pixelMoments;
				r1[s] = Math.sqrt(I2[s] / (cS * vW * vH * vW * vH));
				r2[s] = Math.sqrt(I1[s] / (cS * vW * vH * vW * vH));
				Z1[s] = I1[s] / maxRad2[s];
				Z2[s] = I2[s] / maxRad1[s];
				Zp[s] = (I1[s] + I2[s]) / maxRadC[s];
			} else {
				I1[s] = Double.NaN;
				I2[s] = Double.NaN;
				Ip[s] = Double.NaN;
				r1[s] = Double.NaN;
				r2[s] = Double.NaN;
				maxRad2[s] = Double.NaN;
				maxRad1[s] = Double.NaN;
				Z1[s] = Double.NaN;
				Z2[s] = Double.NaN;
				Zp[s] = Double.NaN;
			}
		}

		double[][] result = { I1, I2, Ip, r1, r2, maxRad2, maxRad1, Z1, Z2, Zp, };

		return result;
	}

	/**
	 * Calculate 3D Local Thickness and determine thickness statistics for the
	 * slice
	 * 
	 */
	private void calculateThickness3D(ImagePlus imp, double min, double max) {
		this.maxCortThick3D = new double[this.al];
		this.meanCortThick3D = new double[this.al];
		this.stdevCortThick3D = new double[this.al];
		Rectangle r = imp.getProcessor().getRoi();
		Thickness th = new Thickness();

		// convert to binary
		ImagePlus binaryImp = convertToBinary(imp, min, max);

		ImagePlus thickImp = th.getLocalThickness(binaryImp, false);

		for (int s = this.startSlice; s <= this.endSlice; s++) {
			if (this.emptySlices[s]) {
				this.maxCortThick3D[s] = Double.NaN;
				this.meanCortThick3D[s] = Double.NaN;
				this.stdevCortThick3D[s] = Double.NaN;
				continue;
			}
			FloatProcessor ip = (FloatProcessor) thickImp.getStack()
					.getProcessor(s);
			double sumPix = 0;
			double sliceMax = 0;
			double pixCount = 0;
			final int roiXEnd = r.x + r.width;
			final int roiYEnd = r.y + r.height;
			for (int y = r.y; y < roiYEnd; y++) {
				for (int x = r.x; x < roiXEnd; x++) {
					final float pixel = Float.intBitsToFloat(ip.get(x, y));
					if (pixel > 0) {
						pixCount++;
						sumPix += pixel;
						sliceMax = Math.max(sliceMax, pixel);
					}
				}
			}
			final double sliceMean = sumPix / pixCount;
			this.meanCortThick3D[s] = sliceMean;
			this.maxCortThick3D[s] = sliceMax;

			double sumSquares = 0;
			for (int y = r.y; y < roiYEnd; y++) {
				for (int x = r.x; x < roiXEnd; x++) {
					final float pixel = Float.intBitsToFloat(ip.get(x, y));
					if (pixel > 0) {
						final double d = sliceMean - pixel;
						sumSquares += d * d;
					}
				}
			}
			this.stdevCortThick3D[s] = Math.sqrt(sumSquares / pixCount);
		}
		return;
	}

	/**
	 * Calculate thickness on individual slices using local thickness
	 * 
	 * @param imp
	 */
	private void calculateThickness2D(ImagePlus imp, double min, double max) {
		this.maxCortThick2D = new double[this.al];
		this.meanCortThick2D = new double[this.al];
		this.stdevCortThick2D = new double[this.al];

		int nThreads = Runtime.getRuntime().availableProcessors();
		SliceThread[] sliceThread = new SliceThread[nThreads];
		for (int thread = 0; thread < nThreads; thread++) {
			sliceThread[thread] = new SliceThread(thread, nThreads, imp, min,
					max, this.meanCortThick2D, this.maxCortThick2D,
					this.stdevCortThick2D, this.startSlice, this.endSlice,
					this.emptySlices);
			sliceThread[thread].start();
		}
		try {
			for (int thread = 0; thread < nThreads; thread++) {
				sliceThread[thread].join();
			}
		} catch (InterruptedException ie) {
			IJ.error("A thread was interrupted.");
		}
		return;
	}

	class SliceThread extends Thread {
		final int thread, nThreads, width, height, startSlice, endSlice;

		double min, max;

		double[] meanThick, maxThick, stdevThick;

		boolean[] emptySlices;

		final ImagePlus impT;

		public SliceThread(int thread, int nThreads, ImagePlus imp, double min,
				double max, double[] meanThick, double[] maxThick,
				double[] stdevThick, int startSlice, int endSlice,
				boolean[] emptySlices) {
			this.impT = imp;
			this.min = min;
			this.max = max;
			this.width = this.impT.getWidth();
			this.height = this.impT.getHeight();
			this.thread = thread;
			this.nThreads = nThreads;
			this.meanThick = meanThick;
			this.maxThick = maxThick;
			this.stdevThick = stdevThick;
			this.startSlice = startSlice;
			this.endSlice = endSlice;
			this.emptySlices = emptySlices;
		}

		public void run() {
			for (int s = this.thread + this.startSlice; s <= this.endSlice; s += this.nThreads) {
				if (this.emptySlices[s]) {
					this.meanThick[s] = Double.NaN;
					this.maxThick[s] = Double.NaN;
					this.stdevThick[s] = Double.NaN;
					continue;
				}
				ImageProcessor ip = impT.getImageStack().getProcessor(s);
				ImagePlus sliceImp = new ImagePlus(" " + s, ip);
				Rectangle r = ip.getRoi();
				// binarise
				ImagePlus binaryImp = convertToBinary(sliceImp, min, max);
				Calibration cal = impT.getCalibration();
				binaryImp.setCalibration(cal);
				// calculate thickness
				Thickness th = new Thickness();
				ImagePlus thickImp = th.getLocalThickness(binaryImp, false);
				FloatProcessor thickIp = (FloatProcessor) thickImp
						.getProcessor();
				double sumPix = 0;
				double sliceMax = 0;
				double pixCount = 0;
				final double roiXEnd = r.x + r.width;
				final double roiYEnd = r.y + r.height;
				for (int y = r.y; y < roiYEnd; y++) {
					for (int x = r.x; x < roiXEnd; x++) {
						final float pixel = Float.intBitsToFloat(thickIp.get(x,
								y));
						if (pixel > 0) {
							pixCount++;
							sumPix += pixel;
							sliceMax = Math.max(sliceMax, pixel);
						}
					}
				}
				final double sliceMean = sumPix / pixCount;
				this.meanThick[s] = sliceMean;
				this.maxThick[s] = sliceMax;

				double sumSquares = 0;
				for (int y = r.y; y < roiYEnd; y++) {
					for (int x = r.x; x < roiXEnd; x++) {
						final float pixel = Float.intBitsToFloat(thickIp.get(x,
								y));
						if (pixel > 0) {
							final double d = sliceMean - pixel;
							sumSquares += d * d;
						}
					}
				}
				this.stdevThick[s] = Math.sqrt(sumSquares / pixCount);
			}
			return;
		}
	}

	private ImagePlus convertToBinary(ImagePlus imp, double min, double max) {
		final int w = imp.getWidth();
		final int h = imp.getHeight();
		final int d = imp.getStackSize();
		final ImageStack sourceStack = imp.getImageStack();
		ImageStack binaryStack = new ImageStack(w, h);
		for (int s = 1; s <= d; s++) {
			ImageProcessor sliceIp = sourceStack.getProcessor(s);
			ByteProcessor binaryIp = new ByteProcessor(w, h);
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < w; x++) {
					if (sliceIp.get(x, y) >= min && sliceIp.get(x, y) <= max) {
						binaryIp.set(x, y, 255);
					} else {
						binaryIp.set(x, y, 0);
					}
				}
			}
			binaryStack.addSlice(sourceStack.getSliceLabel(s), binaryIp);
		}
		ImagePlus binaryImp = new ImagePlus("binaryImp", binaryStack);
		binaryImp.setCalibration(imp.getCalibration());
		return binaryImp;
	}

	private void roiMeasurements(ImagePlus imp, double min, double max) {
		Roi initialRoi = imp.getRoi();
		double[] feretValues = new double[3];
		this.feretAngle = new double[this.al];
		this.feretMax = new double[this.al];
		this.feretMin = new double[this.al];
		this.perimeter = new double[this.al];
		this.principalDiameter = new double[this.al];
		this.secondaryDiameter = new double[this.al];
		int initialSlice = imp.getCurrentSlice();
		// for the required slices...
		for (int s = this.startSlice; s <= this.endSlice; s++) {
			ImageProcessor ip = imp.getImageStack().getProcessor(s);
			Wand w = new Wand(ip);
			w.autoOutline(0, (int) Math.round(this.sliceCentroids[1][s]
					/ this.vH), min, max, Wand.EIGHT_CONNECTED);
			if (this.emptySlices[s] || w.npoints == 0) {
				this.feretMin[s] = Double.NaN;
				this.feretAngle[s] = Double.NaN;
				this.feretMax[s] = Double.NaN;
				this.perimeter[s] = Double.NaN;
				this.principalDiameter[s] = Double.NaN;
				this.secondaryDiameter[s] = Double.NaN;
				continue;
			} else {
				int type = Wand.allPoints() ? Roi.FREEROI : Roi.TRACED_ROI;
				PolygonRoi roi = new PolygonRoi(w.xpoints, w.ypoints,
						w.npoints, type);
				feretValues = roi.getFeretValues();
				this.feretMin[s] = feretValues[2] * this.vW;
				this.feretAngle[s] = feretValues[1] * Math.PI / 180;
				this.feretMax[s] = feretValues[0] * this.vW;
				this.perimeter[s] = roi.getLength() * this.vW;
			}
			if (this.doOriented && orienteer != null) {
				double[][] points = new double[w.npoints][2];
				for (int i = 0; i < w.npoints; i++) {
					points[i][0] = w.xpoints[i] * this.vW;
					points[i][1] = w.ypoints[i] * this.vH;
				}
				double[] diameters = orienteer.getDiameters(points);
				this.principalDiameter[s] = diameters[0];
				this.secondaryDiameter[s] = diameters[1];
			}
			feretValues = null;
		}
		IJ.setSlice(initialSlice);
		imp.setRoi(initialRoi);
		return;
	}

	public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
		if (!DialogModifier.allNumbersValid(gd.getNumericFields()))
			return false;
		Vector<?> checkboxes = gd.getCheckboxes();
		Vector<?> nFields = gd.getNumericFields();
		Checkbox calibration = (Checkbox) checkboxes.get(9);
		boolean isHUCalibrated = calibration.getState();
		TextField minT = (TextField) nFields.get(0);
		TextField maxT = (TextField) nFields.get(1);

		double min = Double.parseDouble(minT.getText());
		double max = Double.parseDouble(maxT.getText());
		if (isHUCalibrated && !fieldUpdated) {
			minT.setText("" + cal.getCValue(min));
			maxT.setText("" + cal.getCValue(max));
			fieldUpdated = true;
		}
		if (!isHUCalibrated && fieldUpdated) {
			minT.setText("" + cal.getRawValue(min));
			maxT.setText("" + cal.getRawValue(max));
			fieldUpdated = false;
		}
		if (isHUCalibrated)
			DialogModifier.replaceUnitString(gd, "grey", "HU");
		else
			DialogModifier.replaceUnitString(gd, "HU", "grey");

		Checkbox oriented = (Checkbox) checkboxes.get(8);
		if (orienteer == null) {
			oriented.setState(false);
			oriented.setEnabled(false);
		} else
			oriented.setEnabled(true);

		DialogModifier.registerMacroValues(gd, gd.getComponents());
		return true;
	}
}

>>>>>>> 76aa07461566a5976980e6696204781271955163
