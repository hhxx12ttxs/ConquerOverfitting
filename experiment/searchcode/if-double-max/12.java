<<<<<<< HEAD
package redis.clients.jedis;

import static redis.clients.jedis.Protocol.toByteArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.util.SafeEncoder;

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
    
    /**
     * Submits a script for pipelined execution
     * @param script The text of the lua script to execute
     * @param keyCount The numbers of keys submitted
     * @param params The keys and/or arguments submitted to the script
     * @return a response containing the return value[s] of the script in a list
     */
    public Response<List<Object>> eval(String script, int keyCount, String... params) {
        client.eval(script, keyCount, params);       
        return new Response<List<Object>>(new MultiResponseBuilder());
    }
   
    public Object eval(String script) {
        client.eval(script, 0);       
        return getEvalResult();
    }
   
    public Object eval(String script, List<String> keys, List<String> args) {
        return eval(script, keys.size(), getParams(keys, args));
    }
   
    public Object evalsha(String sha1, int keyCount, String... params) {
        client.evalsha(sha1, keyCount, params);       
        return getEvalResult();
    }
   
    public Object evalsha(String sha1) {
        client.evalsha(sha1, 0);       
        return getEvalResult();
    }
   
    public Object evalsha(String sha1, List<String> keys, List<String> args) {
        return evalsha(sha1, keys.size(), getParams(keys, args));
    }
   
   
   
    private Object getEvalResult() {
        Object result = client.getOne();

        if (result instanceof byte[])
            return SafeEncoder.encode((byte[]) result);

        if (result instanceof List<?>) {
            List<?> list = (List<?>) result;
            List<String> listResult = new ArrayList<String>(list.size());
            for (Object bin : list)
                listResult.add(SafeEncoder.encode((byte[]) bin));

            return listResult;
        }
        return result;
    }
   
    private String[] getParams(List<String> keys, List<String> args) {
        int keyCount = keys.size();
        int argCount = args.size();

        String[] params = new String[keyCount + args.size()];

        for (int i = 0; i < keyCount; i++)
            params[i] = keys.get(i);

        for (int i = 0; i < argCount; i++)
            params[keyCount + i] = args.get(i);

        return params;
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
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.mahout.math;

import org.apache.mahout.math.function.Functions;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public final class VectorTest extends MahoutTestCase {

  @Test
  public void testSparseVector()  {
    Vector vec1 = new RandomAccessSparseVector(3);
    Vector vec2 = new RandomAccessSparseVector(3);
    doTestVectors(vec1, vec2);
  }

  @Test
  public void testSparseVectorFullIteration() {
    int[] index = {0, 1, 2, 3, 4, 5};
    double[] values = {1, 2, 3, 4, 5, 6};

    assertEquals(index.length, values.length);

    int n = index.length;

    Vector vector = new SequentialAccessSparseVector(n);
    for (int i = 0; i < n; i++) {
      vector.set(index[i], values[i]);
    }

    for (int i = 0; i < n; i++) {
      assertEquals(vector.get(i), values[i], EPSILON);
    }

    int elements = 0;
    for (Vector.Element e : vector) {
      elements++;
    }
    assertEquals(n, elements);

    Vector empty = new SequentialAccessSparseVector(0);
    assertFalse(empty.iterator().hasNext());
  }

  @Test
  public void testSparseVectorSparseIteration() {
    int[] index = {0, 1, 2, 3, 4, 5};
    double[] values = {1, 2, 3, 4, 5, 6};

    assertEquals(index.length, values.length);

    int n = index.length;

    Vector vector = new SequentialAccessSparseVector(n);
    for (int i = 0; i < n; i++) {
      vector.set(index[i], values[i]);
    }

    for (int i = 0; i < n; i++) {
      assertEquals(vector.get(i), values[i], EPSILON);
    }

    int elements = 0;
    Iterator<Vector.Element> it = vector.iterateNonZero();
    while (it.hasNext()) {
      it.next();
      elements++;
    }
    assertEquals(n, elements);

    Vector empty = new SequentialAccessSparseVector(0);
    assertFalse(empty.iterateNonZero().hasNext());
  }

  @Test
  public void testEquivalent()  {
    //names are not used for equivalent
    RandomAccessSparseVector randomAccessLeft = new RandomAccessSparseVector(3);
    Vector sequentialAccessLeft = new SequentialAccessSparseVector(3);
    Vector right = new DenseVector(3);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    randomAccessLeft.setQuick(2, 3);
    sequentialAccessLeft.setQuick(0,1);
    sequentialAccessLeft.setQuick(1,2);
    sequentialAccessLeft.setQuick(2,3);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    assertEquals(randomAccessLeft, right);
    assertEquals(sequentialAccessLeft, right);
    assertEquals(sequentialAccessLeft, randomAccessLeft);

    Vector leftBar = new DenseVector(3);
    leftBar.setQuick(0, 1);
    leftBar.setQuick(1, 2);
    leftBar.setQuick(2, 3);
    assertEquals(leftBar, right);
    assertEquals(randomAccessLeft, right);
    assertEquals(sequentialAccessLeft, right);

    Vector rightBar = new RandomAccessSparseVector(3);
    rightBar.setQuick(0, 1);
    rightBar.setQuick(1, 2);
    rightBar.setQuick(2, 3);
    assertEquals(randomAccessLeft, rightBar);

    right.setQuick(2, 4);
    assertFalse(randomAccessLeft.equals(right));
    right = new DenseVector(4);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    right.setQuick(3, 3);
    assertFalse(randomAccessLeft.equals(right));
    randomAccessLeft = new RandomAccessSparseVector(2);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    assertFalse(randomAccessLeft.equals(right));

    Vector dense = new DenseVector(3);
    right = new DenseVector(3);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    right.setQuick(2, 3);
    dense.setQuick(0, 1);
    dense.setQuick(1, 2);
    dense.setQuick(2, 3);
    assertEquals(dense, right);

    RandomAccessSparseVector sparse = new RandomAccessSparseVector(3);
    randomAccessLeft = new RandomAccessSparseVector(3);
    sparse.setQuick(0, 1);
    sparse.setQuick(1, 2);
    sparse.setQuick(2, 3);
    randomAccessLeft.setQuick(0, 1);
    randomAccessLeft.setQuick(1, 2);
    randomAccessLeft.setQuick(2, 3);
    assertEquals(randomAccessLeft, sparse);

    Vector v1 = new VectorView(randomAccessLeft, 0, 2);
    Vector v2 = new VectorView(right, 0, 2);
    assertEquals(v1, v2);
    sparse = new RandomAccessSparseVector(2);
    sparse.setQuick(0, 1);
    sparse.setQuick(1, 2);
    assertEquals(v1, sparse);
  }

  private static void doTestVectors(Vector left, Vector right) {
    left.setQuick(0, 1);
    left.setQuick(1, 2);
    left.setQuick(2, 3);
    right.setQuick(0, 4);
    right.setQuick(1, 5);
    right.setQuick(2, 6);
    double result = left.dot(right);
    assertEquals(32.0, result, EPSILON);
  }

  @Test
  public void testGetDistanceSquared()  {
    Vector v = new DenseVector(5);
    Vector w = new DenseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);

    v = new RandomAccessSparseVector(5);
    w = new RandomAccessSparseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);

    v = new SequentialAccessSparseVector(5);
    w = new SequentialAccessSparseVector(5);
    setUpV(v);
    setUpW(w);
    doTestGetDistanceSquared(v, w);
    
  }

  @Test
  public void testAddTo() throws Exception {
    Vector v = new DenseVector(4);
    Vector w = new DenseVector(4);
    v.setQuick(0, 1);
    v.setQuick(1, 2);
    v.setQuick(2, 0);
    v.setQuick(3, 4);

    w.setQuick(0, 1);
    w.setQuick(1, 1);
    w.setQuick(2, 1);
    w.setQuick(3, 1);

    v.addTo(w);
    Vector gold = new DenseVector(new double[]{2, 3, 1, 5});
    assertEquals(w, gold);
    assertFalse(v.equals(gold));
  }


  private static void setUpV(Vector v) {
    v.setQuick(1, 2);
    v.setQuick(2, -4);
    v.setQuick(3, -9);
  }

  private static void setUpW(Vector w) {
    w.setQuick(0, -5);
    w.setQuick(1, -1);
    w.setQuick(2, 9);
    w.setQuick(3, 0.1);
    w.setQuick(4, 2.1);
  }

  private static void doTestGetDistanceSquared(Vector v, Vector w) {
    double expected = v.minus(w).getLengthSquared();
    assertEquals(expected, v.getDistanceSquared(w), 1.0e-6);
  }

  @Test
  public void testGetLengthSquared()  {
    Vector v = new DenseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
    v = new RandomAccessSparseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
    v = new SequentialAccessSparseVector(5);
    setUpV(v);
    doTestGetLengthSquared(v);
  }

  public static double lengthSquaredSlowly(Vector v) {
    double d = 0.0;
    for (int i = 0; i < v.size(); i++) {
      double value = v.get(i);
      d += value * value;
    }
    return d;
  }

  private static void doTestGetLengthSquared(Vector v) {
    double expected = lengthSquaredSlowly(v);
    assertEquals("v.getLengthSquared() != sum_of_squared_elements(v)", expected, v.getLengthSquared(), 0.0);

    v.set(v.size()/2, v.get(v.size()/2) + 1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via set() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.setQuick(v.size()/5, v.get(v.size()/5) + 1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via setQuick() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    Iterator<Vector.Element> it = v.iterator();
    while (it.hasNext()) {
      Vector.Element e = it.next();
      if (e.index() == v.size() - 2) {
        e.set(e.get() - 5.0);
      }
    }
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via dense iterator.set fails to change lengthSquared",
                 expected, v.getLengthSquared(), EPSILON);

    it = v.iterateNonZero();
    int i = 0;
    while (it.hasNext()) {
      i++;
      Vector.Element e = it.next();
      if (i == v.getNumNondefaultElements() - 1) {
        e.set(e.get() - 5.0);
      }
    }
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via sparse iterator.set fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(3.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(Functions.SQUARE);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(square) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(new double[v.size()]);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(double[]) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.getElement(v.size()/2).set(2.5);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via v.getElement().set() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.normalize();
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via normalize() fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.set(0, 1.5);
    v.normalize(1.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via normalize(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.times(2.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via times(double) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.times(v);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via times(vector) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(Functions.POW, 3.0);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(pow, 3.0) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);

    v.assign(v, Functions.PLUS);
    expected = lengthSquaredSlowly(v);
    assertEquals("mutation via assign(v,plus) fails to change lengthSquared", expected, v.getLengthSquared(), EPSILON);
  }

  @Test
  public void testIterator() {

    Collection<Integer> expectedIndices = new HashSet<Integer>();
    int i = 1;
    while (i <= 20) {
      expectedIndices.add(i * (i + 1) / 2);
      i++;
    }

    Vector denseVector = new DenseVector(i * i);
    for (int index : expectedIndices) {
      denseVector.set(index, (double) 2 * index);
    }
    doTestIterators(denseVector, expectedIndices);

    Vector randomAccessVector = new RandomAccessSparseVector(i * i);
    for (int index : expectedIndices) {
      randomAccessVector.set(index, (double) 2 * index);
    }
    doTestIterators(randomAccessVector, expectedIndices);

    Vector sequentialVector = new SequentialAccessSparseVector(i * i);
    for (int index : expectedIndices) {
      sequentialVector.set(index, (double) 2 * index);
    }
    doTestIterators(sequentialVector, expectedIndices);
  }

  private static void doTestIterators(Vector vector, Collection<Integer> expectedIndices) {
    expectedIndices = new HashSet<Integer>(expectedIndices);
    Iterator<Vector.Element> allIterator = vector.iterator();
    int index = 0;
    while (allIterator.hasNext()) {
      Vector.Element element = allIterator.next();
      assertEquals(index, element.index());
      if (expectedIndices.contains(index)) {
        assertEquals((double) index * 2, element.get(), EPSILON);
      } else {
        assertEquals(0.0, element.get(), EPSILON);
      }
      index++;
    }

    Iterator<Vector.Element> nonZeroIterator = vector.iterateNonZero();
    while (nonZeroIterator.hasNext()) {
      Vector.Element element = nonZeroIterator.next();
      index = element.index();
      assertTrue(expectedIndices.contains(index));
      assertEquals((double) index * 2, element.get(), EPSILON);
      expectedIndices.remove(index);
    }
    assertTrue(expectedIndices.isEmpty());
  }

  @Test
  public void testNormalize()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, 1);
    vec1.setQuick(1, 2);
    vec1.setQuick(2, 3);
    Vector norm = vec1.normalize();
    assertNotNull("norm1 is null and it shouldn't be", norm);

    Vector vec2 = new SequentialAccessSparseVector(3);

    vec2.setQuick(0, 1);
    vec2.setQuick(1, 2);
    vec2.setQuick(2, 3);
    Vector norm2 = vec2.normalize();
    assertNotNull("norm1 is null and it shouldn't be", norm2);

    Vector expected = new RandomAccessSparseVector(3);

    expected.setQuick(0, 0.2672612419124244);
    expected.setQuick(1, 0.5345224838248488);
    expected.setQuick(2, 0.8017837257372732);

    assertEquals(expected, norm);

    norm = vec1.normalize(2);
    assertEquals(expected, norm);

    norm2 = vec2.normalize(2);
    assertEquals(expected, norm2);

    norm = vec1.normalize(1);
    norm2 = vec2.normalize(1);
    expected.setQuick(0, 1.0 / 6);
    expected.setQuick(1, 2.0 / 6);
    expected.setQuick(2, 3.0 / 6);
    assertEquals(expected, norm);
    assertEquals(expected, norm2);
    norm = vec1.normalize(3);
    //expected = vec1.times(vec1).times(vec1);

    // double sum = expected.zSum();
    // cube = Math.pow(sum, 1.0/3);
    double cube = Math.pow(36, 1.0 / 3);
    expected = vec1.divide(cube);

    assertEquals(norm, expected);

    norm = vec1.normalize(Double.POSITIVE_INFINITY);
    norm2 = vec2.normalize(Double.POSITIVE_INFINITY);
    // The max is 3, so we divide by that.
    expected.setQuick(0, 1.0 / 3);
    expected.setQuick(1, 2.0 / 3);
    expected.setQuick(2, 3.0 / 3);
    assertEquals(norm, expected);
    assertEquals(norm2, expected);

    norm = vec1.normalize(0);
    // The max is 3, so we divide by that.
    expected.setQuick(0, 1.0 / 3);
    expected.setQuick(1, 2.0 / 3);
    expected.setQuick(2, 3.0 / 3);
    assertEquals(norm, expected);

    try {
      vec1.normalize(-1);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      vec2.normalize(-1);
      fail();
    } catch (IllegalArgumentException e) {
      // expected
    }
  }
  
  @Test
  public void testLogNormalize() {
    Vector vec1 = new RandomAccessSparseVector(3);
    
    vec1.setQuick(0, 1);
    vec1.setQuick(1, 2);
    vec1.setQuick(2, 3);
    Vector norm = vec1.logNormalize();
    assertNotNull("norm1 is null and it shouldn't be", norm);
    
    Vector vec2 = new SequentialAccessSparseVector(3);
    
    vec2.setQuick(0, 1);
    vec2.setQuick(1, 2);
    vec2.setQuick(2, 3);
    Vector norm2 = vec2.logNormalize();
    assertNotNull("norm1 is null and it shouldn't be", norm2);

    Vector expected = new DenseVector(new double[]{
      0.2672612419124244, 0.4235990463273581, 0.5345224838248488
    });

    assertVectorEquals(expected, norm, 1.0e-15);
    assertVectorEquals(expected, norm2, 1.0e-15);

    norm = vec1.logNormalize(2);
    assertVectorEquals(expected, norm, 1.0e-15);
    
    norm2 = vec2.logNormalize(2);
    assertVectorEquals(expected, norm2, 1.0e-15);
    
    try {
      vec1.logNormalize(1);
      fail("Should fail with power == 1");
    } catch (IllegalArgumentException e) {
      // expected
    }

    try {
      vec1.logNormalize(-1);
      fail("Should fail with negative power");
    } catch (IllegalArgumentException e) {
      // expected
    }
    
    try {
      norm = vec2.logNormalize(Double.POSITIVE_INFINITY);
      fail("Should fail with positive infinity norm");
    } catch (IllegalArgumentException e) {
      // expected
    }  
  }

  private static void assertVectorEquals(Vector expected, Vector actual, double epsilon) {
    assertEquals(expected.size(), actual.size());
    for (Vector.Element x : expected) {
      assertEquals(x.get(), actual.get(x.index()), epsilon);
    }
  }

  @Test
  public void testMax()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(1, -3);
    vec1.setQuick(2, -2);

    double max = vec1.maxValue();
    assertEquals(max + " does not equal: " + (-1.0), -1.0, max, 0.0);

    int idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 0, 0, idx);

    vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(max + " does not equal: " + 0, 0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 1, 1, idx);
    
    vec1 = new SequentialAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(max + " does not equal: " + 0, 0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 1, 1, idx);
    
    vec1 = new DenseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);
    
    max = vec1.maxValue();
    assertEquals(max + " does not equal: " + 0, 0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 1, 1, idx);
    
    vec1 = new RandomAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(max + " does not equal 0", 0.0, max, EPSILON);

    vec1 = new DenseVector(3);
    max = vec1.maxValue();
    assertEquals(max + " does not equal 0", 0.0, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(max + " does not equal 0", 0.0, max, EPSILON);

    vec1 = new RandomAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(max + " does not equal -inf", Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new DenseVector(0);
    max = vec1.maxValue();
    assertEquals(max + " does not equal -inf", Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(max + " does not equal -inf", Double.NEGATIVE_INFINITY, max, EPSILON);

  }

  @Test
  public void testMin()  {
    Vector vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, 1);
    vec1.setQuick(1, 3);
    vec1.setQuick(2, 2);

    double max = vec1.minValue();
    assertEquals(max + " does not equal: " + (1.0), 1.0, max, 0.0);

    int idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 1, 1, idx);

    vec1 = new RandomAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(max + " does not equal: " + 0, 0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 1, 1, idx);

    vec1 = new SequentialAccessSparseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(max + " does not equal: " + 0, 0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 1, 1, idx);

    vec1 = new DenseVector(3);

    vec1.setQuick(0, -1);
    vec1.setQuick(2, -2);

    max = vec1.maxValue();
    assertEquals(max + " does not equal: " + 0, 0.0, max, 0.0);

    idx = vec1.maxValueIndex();
    assertEquals(idx + " does not equal: " + 1, 1, idx);

    vec1 = new RandomAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(max + " does not equal 0", 0.0, max, EPSILON);

    vec1 = new DenseVector(3);
    max = vec1.maxValue();
    assertEquals(max + " does not equal 0", 0.0, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(3);
    max = vec1.maxValue();
    assertEquals(max + " does not equal 0", 0.0, max, EPSILON);

    vec1 = new RandomAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(max + " does not equal -inf", Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new DenseVector(0);
    max = vec1.maxValue();
    assertEquals(max + " does not equal -inf", Double.NEGATIVE_INFINITY, max, EPSILON);

    vec1 = new SequentialAccessSparseVector(0);
    max = vec1.maxValue();
    assertEquals(max + " does not equal -inf", Double.NEGATIVE_INFINITY, max, EPSILON);

  }

  @Test
  public void testDenseVector()  {
    Vector vec1 = new DenseVector(3);
    Vector vec2 = new DenseVector(3);
    doTestVectors(vec1, vec2);
  }

  @Test
  public void testVectorView()  {
    RandomAccessSparseVector vec1 = new RandomAccessSparseVector(3);
    RandomAccessSparseVector vec2 = new RandomAccessSparseVector(6);
    SequentialAccessSparseVector vec3 = new SequentialAccessSparseVector(3);
    SequentialAccessSparseVector vec4 = new SequentialAccessSparseVector(6);
    Vector vecV1 = new VectorView(vec1, 0, 3);
    Vector vecV2 = new VectorView(vec2, 2, 3);
    Vector vecV3 = new VectorView(vec3, 0, 3);
    Vector vecV4 = new VectorView(vec4, 2, 3);
    doTestVectors(vecV1, vecV2);
    doTestVectors(vecV3, vecV4);
  }

  /** Asserts a vector using enumeration equals a given dense vector */
  private static void doTestEnumeration(double[] apriori, Vector vector) {
    double[] test = new double[apriori.length];
    Iterator<Vector.Element> iter = vector.iterateNonZero();
    while (iter.hasNext()) {
      Vector.Element e = iter.next();
      test[e.index()] = e.get();
    }

    for (int i = 0; i < test.length; i++) {
      assertEquals(apriori[i], test[i], EPSILON);
    }
  }

  @Test
  public void testEnumeration()  {
    double[] apriori = {0, 1, 2, 3, 4};

    doTestEnumeration(apriori, new VectorView(new DenseVector(new double[]{
        -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9}), 2, 5));

    doTestEnumeration(apriori, new DenseVector(new double[]{0, 1, 2, 3, 4}));

    Vector sparse = new RandomAccessSparseVector(5);
    sparse.set(0, 0);
    sparse.set(1, 1);
    sparse.set(2, 2);
    sparse.set(3, 3);
    sparse.set(4, 4);
    doTestEnumeration(apriori, sparse);

    sparse = new SequentialAccessSparseVector(5);
    sparse.set(0, 0);
    sparse.set(1, 1);
    sparse.set(2, 2);
    sparse.set(3, 3);
    sparse.set(4, 4);
    doTestEnumeration(apriori, sparse);

  }

  @Test
  public void testAggregation()  {
    Vector v = new DenseVector(5);
    Vector w = new DenseVector(5);
    setUpFirstVector(v);
    setUpSecondVector(w);
    doTestAggregation(v, w);
    v = new RandomAccessSparseVector(5);
    w = new RandomAccessSparseVector(5);
    setUpFirstVector(v);
    doTestAggregation(v, w);
    setUpSecondVector(w);
    doTestAggregation(w, v);
    v = new SequentialAccessSparseVector(5);
    w = new SequentialAccessSparseVector(5);
    setUpFirstVector(v);
    doTestAggregation(v, w);
    setUpSecondVector(w);
    doTestAggregation(w, v);
  }

  private static void doTestAggregation(Vector v, Vector w) {
    assertEquals("aggregate(plus, pow(2)) not equal to " + v.getLengthSquared(),
        v.getLengthSquared(),
        v.aggregate(Functions.PLUS, Functions.pow(2)), EPSILON);
    assertEquals("aggregate(plus, abs) not equal to " + v.norm(1),
        v.norm(1),
        v.aggregate(Functions.PLUS, Functions.ABS), EPSILON);
    assertEquals("aggregate(max, abs) not equal to " + v.norm(Double.POSITIVE_INFINITY),
        v.norm(Double.POSITIVE_INFINITY),
        v.aggregate(Functions.MAX, Functions.ABS), EPSILON);

    assertEquals("v.dot(w) != v.aggregate(w, plus, mult)",
        v.dot(w),
        v.aggregate(w, Functions.PLUS, Functions.MULT), EPSILON);
    assertEquals("|(v-w)|^2 != v.aggregate(w, plus, chain(pow(2), minus))",
        v.minus(w).dot(v.minus(w)),
        v.aggregate(w, Functions.PLUS, Functions.chain(Functions.pow(2), Functions.MINUS)), EPSILON);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAggregate1() {
    assertEquals(1.0, new DenseVector(new double[]{1}).aggregate(Functions.MIN, Functions.IDENTITY), EPSILON);
    assertEquals(1.0, new DenseVector(new double[]{2, 1}).aggregate(Functions.MIN, Functions.IDENTITY), EPSILON);
    new DenseVector(new double[0]).aggregate(Functions.MIN, Functions.IDENTITY);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testEmptyAggregate2() {
    assertEquals(3.0, new DenseVector(new double[]{1}).aggregate(
        new DenseVector(new double[]{2}),Functions.MIN, Functions.PLUS), EPSILON);
    new DenseVector(new double[0]).aggregate(new DenseVector(new double[0]), Functions.MIN, Functions.PLUS);
  }

  private static void setUpFirstVector(Vector v) {
    v.setQuick(1, 2);
    v.setQuick(2, 0.5);
    v.setQuick(3, -5);
  }

  private static void setUpSecondVector(Vector v) {
    v.setQuick(0, 3);
    v.setQuick(1, -1.5);
    v.setQuick(2, -5);
    v.setQuick(3, 2);
  }

  @Test
  public void testHashCodeEquivalence() {
    // Hash codes must be equal if the vectors are considered equal
    Vector sparseLeft = new RandomAccessSparseVector(3);
    Vector denseRight = new DenseVector(3);
    sparseLeft.setQuick(0, 1);
    sparseLeft.setQuick(1, 2);
    sparseLeft.setQuick(2, 3);
    denseRight.setQuick(0, 1);
    denseRight.setQuick(1, 2);
    denseRight.setQuick(2, 3);
    assertEquals(sparseLeft, denseRight);
    assertEquals(sparseLeft.hashCode(), denseRight.hashCode());

    sparseLeft = new SequentialAccessSparseVector(3);
    sparseLeft.setQuick(0, 1);
    sparseLeft.setQuick(1, 2);
    sparseLeft.setQuick(2, 3);
    assertEquals(sparseLeft, denseRight);
    assertEquals(sparseLeft.hashCode(), denseRight.hashCode());

    Vector denseLeft = new DenseVector(3);
    denseLeft.setQuick(0, 1);
    denseLeft.setQuick(1, 2);
    denseLeft.setQuick(2, 3);
    assertEquals(denseLeft, denseRight);
    assertEquals(denseLeft.hashCode(), denseRight.hashCode());

    Vector sparseRight = new SequentialAccessSparseVector(3);
    sparseRight.setQuick(0, 1);
    sparseRight.setQuick(1, 2);
    sparseRight.setQuick(2, 3);
    assertEquals(sparseLeft, sparseRight);
    assertEquals(sparseLeft.hashCode(), sparseRight.hashCode());

    DenseVector emptyLeft = new DenseVector(0);
    Vector emptyRight = new SequentialAccessSparseVector(0);
    assertEquals(emptyLeft, emptyRight);
    assertEquals(emptyLeft.hashCode(), emptyRight.hashCode());
    emptyRight = new RandomAccessSparseVector(0);
    assertEquals(emptyLeft, emptyRight);
    assertEquals(emptyLeft.hashCode(), emptyRight.hashCode());
  }

  @Test
  public void testHashCode() {
    // Make sure that hash([1,0,2]) != hash([1,2,0])
    Vector left = new SequentialAccessSparseVector(3);
    Vector right = new SequentialAccessSparseVector(3);
    left.setQuick(0, 1);
    left.setQuick(2, 2);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    left = new RandomAccessSparseVector(3);
    right = new RandomAccessSparseVector(3);
    left.setQuick(0, 1);
    left.setQuick(2, 2);
    right.setQuick(0, 1);
    right.setQuick(1, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    // Make sure that hash([1,0,2,0,0,0]) != hash([1,0,2])
    right = new SequentialAccessSparseVector(5);
    right.setQuick(0, 1);
    right.setQuick(2, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());

    right = new RandomAccessSparseVector(5);
    right.setQuick(0, 1);
    right.setQuick(2, 2);
    assertFalse(left.equals(right));
    assertFalse(left.hashCode() == right.hashCode());
  }

}

>>>>>>> 76aa07461566a5976980e6696204781271955163
