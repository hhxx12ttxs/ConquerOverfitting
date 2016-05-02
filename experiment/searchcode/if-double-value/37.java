<<<<<<< HEAD
/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.wst.jsdt.debug.internal.rhino.transport;

import java.math.BigDecimal;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.mozilla.javascript.Undefined;


/**
 * This class provides utilities for working with JSON.
 * <br><br>JSON identifiers map to Java as follows:
 * <ul>
 * <li>string	<--> 	java.lang.String</li>
 * <li>number	<--> 	java.math.Number (BigDecimal)</li>
 * <li>object	<--> 	java.util.Map (HashMap)</li>
 * <li>array	<--> 	java.util.Collection (ArrayList)</li>
 * <li>true		<--> 	java.lang.Boolean.TRUE</li>
 * <li>false	<--> 	java.lang.Boolean.FALSE</li>
 * <li>null		<--> 	null</li>
 * </ul> 
 * 
 * @see JSONConstants
 * @since 1.0
 */
public final class JSONUtil {

	/**
	 * Constructor
	 * no instantiation
	 */
	private JSONUtil() {}
	
	/**
	 * Reads an object from the given JSON string.
	 * <br><br>JSON identifiers map to Java as follows:
	 * <ul>
	 * <li>string	<--> 	java.lang.String</li>
	 * <li>number	<--> 	java.math.Number (BigDecimal)</li>
	 * <li>object	<--> 	java.util.Map (HashMap)</li>
	 * <li>array	<--> 	java.util.Collection (ArrayList)</li>
	 * <li>true		<--> 	java.lang.Boolean.TRUE</li>
	 * <li>false	<--> 	java.lang.Boolean.FALSE</li>
	 * <li>null		<--> 	null</li>
	 * </ul> 
	 * @param jsonString
	 * @return the object corresponding to the JSON string or <code>null</code>
	 */
	public static Object read(String jsonString) {
		return parse(new StringCharacterIterator(jsonString));
	}

	/**
	 * Writes the given object to JSON
	 * <br><br>JSON identifiers map to Java as follows:
	 * <ul>
	 * <li>string	<--> 	java.lang.String</li>
	 * <li>number	<--> 	java.math.Number (BigDecimal)</li>
	 * <li>object	<--> 	java.util.Map (HashMap)</li>
	 * <li>array	<--> 	java.util.Collection (ArrayList)</li>
	 * <li>true		<--> 	java.lang.Boolean.TRUE</li>
	 * <li>false	<--> 	java.lang.Boolean.FALSE</li>
	 * <li>null		<--> 	null</li>
	 * </ul> 
	 * @param jsonObject
	 * @return the composed JSON string, never <code>null</code>
	 */
	public static String write(Object jsonObject) {
		StringBuffer buffer = new StringBuffer();
		writeValue(jsonObject, buffer);
		return buffer.toString();
	}

	/**
	 * Creates an {@link IllegalStateException} for the given message and iterator
	 * 
	 * @param message the message for the exception
	 * @param it the iterator
	 * @return a new {@link IllegalStateException} 
	 */
	private static RuntimeException error(String message, CharacterIterator it) {
		return new IllegalStateException("[" + it.getIndex() + "] " + message); //$NON-NLS-1$//$NON-NLS-2$
	}

	/**
	 * Creates an {@link IllegalStateException} for the given message
	 * 
	 * @param message the message for the exception
	 * @return a new {@link IllegalStateException}
	 */
	private static RuntimeException error(String message) {
		return new IllegalStateException(message);
	}

	/**
	 * Parses the object value from the JSON string.
	 * <br><br>JSON identifiers map to Java as follows:
	 * <ul>
	 * <li>string	<--> 	java.lang.String</li>
	 * <li>number	<--> 	java.math.Number (BigDecimal)</li>
	 * <li>object	<--> 	java.util.Map (HashMap)</li>
	 * <li>array	<--> 	java.util.Collection (ArrayList)</li>
	 * <li>true		<--> 	java.lang.Boolean.TRUE</li>
	 * <li>false	<--> 	java.lang.Boolean.FALSE</li>
	 * <li>null		<--> 	null</li>
	 * </ul> 
	 * @param it
	 * @return
	 */
	private static Object parse(CharacterIterator it) {
		parseWhitespace(it);
		Object result = parseValue(it);
		parseWhitespace(it);

		if (it.current() != CharacterIterator.DONE) {
			throw error("should be done", it); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Chews up whitespace from the iterator
	 * 
	 * @param it
	 */
	private static void parseWhitespace(CharacterIterator it) {
		char c = it.current();
		while (Character.isWhitespace(c)) {
			c = it.next();
		}
	}

	/**
	 * Delegate method that calls the correct parse* method for object creation
	 * <br><br>JSON identifiers map to Java as follows:
	 * <ul>
	 * <li>string	<--> 	java.lang.String</li>
	 * <li>number	<--> 	java.math.Number (BigDecimal)</li>
	 * <li>object	<--> 	java.util.Map (HashMap)</li>
	 * <li>array	<--> 	java.util.Collection (ArrayList)</li>
	 * <li>true		<--> 	java.lang.Boolean.TRUE</li>
	 * <li>false	<--> 	java.lang.Boolean.FALSE</li>
	 * <li>null		<--> 	null</li>
	 * </ul> 
	 * @param it
	 * @return
	 */
	private static Object parseValue(CharacterIterator it) {
		switch (it.current()) {
			case '{' :
				return parseObject(it);
			case '[' :
				return parseArray(it);
			case '"' :
				return parseString(it);
			case '-' :
			case '0' :
			case '1' :
			case '2' :
			case '3' :
			case '4' :
			case '5' :
			case '6' :
			case '7' :
			case '8' :
			case '9' :
				return parseNumber(it);
			case 't' :
				parseText(Boolean.TRUE.toString(), it);
				return Boolean.TRUE;
			case 'f' :
				parseText(Boolean.FALSE.toString(), it);
				return Boolean.FALSE;
			case 'n' :
				parseText(JSONConstants.NULL, it);
				return null;
			case 'u':
				parseText(JSONConstants.UNDEFINED, it);
				return null;
		}
		throw error("Bad JSON starting character '" + it.current() + "'", it); //$NON-NLS-1$ //$NON-NLS-2$;
	}

	/**
	 * Parses an {@link Map} object from the iterator or throws an
	 * {@link IllegalStateException} if parsing fails.
	 * 
	 * @param it
	 * @return a new {@link Map} object, never <code>null</code>
	 */
	private static Map parseObject(CharacterIterator it) {
		it.next();
		parseWhitespace(it);
		if (it.current() == '}') {
			it.next();
			return Collections.EMPTY_MAP;
		}

		Map map = new HashMap();
		while (true) {
			if (it.current() != '"')
				throw error("expected a string start '\"' but was '" + it.current() + "'", it); //$NON-NLS-1$ //$NON-NLS-2$
			String key = parseString(it);
			if (map.containsKey(key))
				throw error("' already defined" + "key '" + key, it); //$NON-NLS-1$ //$NON-NLS-2$
			parseWhitespace(it);
			if (it.current() != ':')
				throw error("expected a pair separator ':' but was '" + it.current() + "'", it); //$NON-NLS-1$ //$NON-NLS-2$
			it.next();
			parseWhitespace(it);
			Object value = parseValue(it);
			map.put(key, value);
			parseWhitespace(it);
			if (it.current() == ',') {
				it.next();
				parseWhitespace(it);
				continue;
			}

			if (it.current() != '}')
				throw error("expected an object close '}' but was '" + it.current() + "'", it); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}
		it.next();
		return map;
	}

	/**
	 * Parses an {@link ArrayList} from the given iterator or throws an
	 * {@link IllegalStateException} if parsing fails
	 * 
	 * @param it
	 * @return a new {@link ArrayList} object never <code>null</code>
	 */
	private static List parseArray(CharacterIterator it) {
		it.next();
		parseWhitespace(it);
		if (it.current() == ']') {
			it.next();
			return Collections.EMPTY_LIST;
		}

		List list = new ArrayList();
		while (true) {
			Object value = parseValue(it);
			list.add(value);
			parseWhitespace(it);
			if (it.current() == ',') {
				it.next();
				parseWhitespace(it);
				continue;
			}

			if (it.current() != ']')
				throw error("expected an array close ']' but was '" + it.current() + "'", it); //$NON-NLS-1$ //$NON-NLS-2$
			break;
		}
		it.next();
		return list;
	}

	private static void parseText(String string, CharacterIterator it) {
		int length = string.length();
		char c = it.current();
		for (int i = 0; i < length; i++) {
			if (c != string.charAt(i))
				throw error("expected to parse '" + string + "' but character " + (i + 1) + " was '" + c + "'", it); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$;
			c = it.next();
		}
	}

	private static Object parseNumber(CharacterIterator it) {
		StringBuffer buffer = new StringBuffer();
		char c = it.current();
		while (Character.isDigit(c) || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
			buffer.append(c);
			c = it.next();
		}
		try {
			return new BigDecimal(buffer.toString());
		} catch (NumberFormatException e) {
			throw error("expected a number but was '" + buffer.toString() + "'", it); //$NON-NLS-1$ //$NON-NLS-2$;
		}
	}

	private static String parseString(CharacterIterator it) {
		char c = it.next();
		if (c == '"') {
			it.next();
			return ""; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		while (c != '"') {
			if (Character.isISOControl(c)) {
				//XXX we should ignore the ISO control chars and make a best effort to continue
				c = it.next();
				continue;
				//throw error("illegal iso control character: '" + Integer.toHexString(c) + "'", it); //$NON-NLS-1$ //$NON-NLS-2$);
			}
			if (c == '\\') {
				c = it.next();
				switch (c) {
					case '"' :
					case '\\' :
					case '/' :
						buffer.append(c);
						break;
					case 'b' :
						buffer.append('\b');
						break;
					case 'f' :
						buffer.append('\f');
						break;
					case 'n' :
						buffer.append('\n');
						break;
					case 'r' :
						buffer.append('\r');
						break;
					case 't' :
						buffer.append('\t');
						break;
					case 'u' :
						StringBuffer unicode = new StringBuffer(4);
						for (int i = 0; i < 4; i++) {
							unicode.append(it.next());
						}
						try {
							buffer.append((char) Integer.parseInt(unicode.toString(), 16));
						} catch (NumberFormatException e) {
							throw error("expected a unicode hex number but was '" + unicode.toString() + "'", it); //$NON-NLS-1$ //$NON-NLS-2$););
						}
						break;
					default :
						throw error("illegal escape character '" + c + "'", it); //$NON-NLS-1$ //$NON-NLS-2$););
				}
			} else
				buffer.append(c);

			c = it.next();
		}
		c = it.next();
		return buffer.toString();
	}

	private static void writeValue(Object value, StringBuffer buffer) {
		if (value == null)
			buffer.append(JSONConstants.NULL);
		else if (value instanceof Boolean)
			buffer.append(value.toString());
		else if (value instanceof Number)
			writeNumber((Number) value, buffer);
		else if (value instanceof String)
			writeString((String) value, buffer);
		else if (value instanceof Collection)
			writeArray((Collection) value, buffer);
		else if (value instanceof Map)
			writeObject((Map) value, buffer);
		else  if(value instanceof Undefined) {
			buffer.append(JSONConstants.UNDEFINED);
		}
		else
			throw error("Unexpected object instance type was '" + value.getClass().getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$););
	}

	private static void writeNumber(Number value, StringBuffer buffer) {
		if (value instanceof Double) {
			if (((Double)value).isNaN() || ((Double)value).isInfinite()) {
				buffer.append(JSONConstants.NULL);
				return;
			}
		} else if (value instanceof Float) {
			if (((Float)value).isNaN() || ((Float)value).isInfinite()) {
				buffer.append(JSONConstants.NULL);
				return;
			}
		}
		buffer.append(value.toString());
	}

	private static void writeObject(Map map, StringBuffer buffer) {
		buffer.append('{');
		for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			if (!(key instanceof String))
				throw error("Map keys must be an instance of String but was '" + key.getClass().getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$););
			writeString((String) key, buffer);
			buffer.append(':');
			writeValue(map.get(key), buffer);
			buffer.append(',');
		}
		if (buffer.charAt(buffer.length() - 1) == ',')
			buffer.setCharAt(buffer.length() - 1, '}');
		else
			buffer.append('}');
	}

	private static void writeArray(Collection collection, StringBuffer buffer) {
		buffer.append('[');
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			writeValue(iterator.next(), buffer);
			buffer.append(',');
		}
		if (buffer.charAt(buffer.length() - 1) == ',')
			buffer.setCharAt(buffer.length() - 1, ']');
		else
			buffer.append(']');
	}

	private static void writeString(String string, StringBuffer buffer) {
		buffer.append('"');
		int length = string.length();
		for (int i = 0; i < length; i++) {
			char c = string.charAt(i);
			switch (c) {
				case '"' :
				case '\\' :
				case '/' :
					buffer.append('\\');
					buffer.append(c);
					break;
				case '\b' :
					buffer.append("\\b"); //$NON-NLS-1$
					break;
				case '\f' :
					buffer.append("\\f"); //$NON-NLS-1$
					break;
				case '\n' :
					buffer.append("\\n"); //$NON-NLS-1$
					break;
				case '\r' :
					buffer.append("\\r"); //$NON-NLS-1$
					break;
				case '\t' :
					buffer.append("\\t"); //$NON-NLS-1$
					break;
				default :
					if (Character.isISOControl(c)) {
						buffer.append("\\u"); //$NON-NLS-1$
						String hexString = Integer.toHexString(c);
						for (int j = hexString.length(); j < 4; j++)
							buffer.append('0');
						buffer.append(hexString);
					} else
						buffer.append(c);
			}
		}
		buffer.append('"');
	}
=======
package redis.clients.jedis;

import java.io.Closeable;
import redis.clients.jedis.BinaryClient.LIST_POSITION;
import redis.clients.util.Hashing;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import redis.clients.util.Pool;

public class ShardedJedis extends BinaryShardedJedis implements JedisCommands,
	Closeable {

    protected Pool<ShardedJedis> dataSource = null;

    public ShardedJedis(List<JedisShardInfo> shards) {
	super(shards);
    }

    public ShardedJedis(List<JedisShardInfo> shards, Hashing algo) {
	super(shards, algo);
    }

    public ShardedJedis(List<JedisShardInfo> shards, Pattern keyTagPattern) {
	super(shards, keyTagPattern);
    }

    public ShardedJedis(List<JedisShardInfo> shards, Hashing algo,
	    Pattern keyTagPattern) {
	super(shards, algo, keyTagPattern);
    }

    public String set(String key, String value) {
	Jedis j = getShard(key);
	return j.set(key, value);
    }

    @Override
    public String set(String key, String value, String nxxx, String expx,
	    long time) {
	Jedis j = getShard(key);
	return j.set(key, value, nxxx, expx, time);
    }

    public String get(String key) {
	Jedis j = getShard(key);
	return j.get(key);
    }

    public String echo(String string) {
	Jedis j = getShard(string);
	return j.echo(string);
    }

    public Boolean exists(String key) {
	Jedis j = getShard(key);
	return j.exists(key);
    }

    public String type(String key) {
	Jedis j = getShard(key);
	return j.type(key);
    }

    public Long expire(String key, int seconds) {
	Jedis j = getShard(key);
	return j.expire(key, seconds);
    }

    public Long expireAt(String key, long unixTime) {
	Jedis j = getShard(key);
	return j.expireAt(key, unixTime);
    }

    public Long ttl(String key) {
	Jedis j = getShard(key);
	return j.ttl(key);
    }

    public Boolean setbit(String key, long offset, boolean value) {
	Jedis j = getShard(key);
	return j.setbit(key, offset, value);
    }

    public Boolean setbit(String key, long offset, String value) {
	Jedis j = getShard(key);
	return j.setbit(key, offset, value);
    }

    public Boolean getbit(String key, long offset) {
	Jedis j = getShard(key);
	return j.getbit(key, offset);
    }

    public Long setrange(String key, long offset, String value) {
	Jedis j = getShard(key);
	return j.setrange(key, offset, value);
    }

    public String getrange(String key, long startOffset, long endOffset) {
	Jedis j = getShard(key);
	return j.getrange(key, startOffset, endOffset);
    }

    public String getSet(String key, String value) {
	Jedis j = getShard(key);
	return j.getSet(key, value);
    }

    public Long setnx(String key, String value) {
	Jedis j = getShard(key);
	return j.setnx(key, value);
    }

    public String setex(String key, int seconds, String value) {
	Jedis j = getShard(key);
	return j.setex(key, seconds, value);
    }

    public List<String> blpop(String arg) {
	Jedis j = getShard(arg);
	return j.blpop(arg);
    }

    public List<String> brpop(String arg) {
	Jedis j = getShard(arg);
	return j.brpop(arg);
    }

    public Long decrBy(String key, long integer) {
	Jedis j = getShard(key);
	return j.decrBy(key, integer);
    }

    public Long decr(String key) {
	Jedis j = getShard(key);
	return j.decr(key);
    }

    public Long incrBy(String key, long integer) {
	Jedis j = getShard(key);
	return j.incrBy(key, integer);
    }

    public Double incrByFloat(String key, double integer) {
	Jedis j = getShard(key);
	return j.incrByFloat(key, integer);
    }

    public Long incr(String key) {
	Jedis j = getShard(key);
	return j.incr(key);
    }

    public Long append(String key, String value) {
	Jedis j = getShard(key);
	return j.append(key, value);
    }

    public String substr(String key, int start, int end) {
	Jedis j = getShard(key);
	return j.substr(key, start, end);
    }

    public Long hset(String key, String field, String value) {
	Jedis j = getShard(key);
	return j.hset(key, field, value);
    }

    public String hget(String key, String field) {
	Jedis j = getShard(key);
	return j.hget(key, field);
    }

    public Long hsetnx(String key, String field, String value) {
	Jedis j = getShard(key);
	return j.hsetnx(key, field, value);
    }

    public String hmset(String key, Map<String, String> hash) {
	Jedis j = getShard(key);
	return j.hmset(key, hash);
    }

    public List<String> hmget(String key, String... fields) {
	Jedis j = getShard(key);
	return j.hmget(key, fields);
    }

    public Long hincrBy(String key, String field, long value) {
	Jedis j = getShard(key);
	return j.hincrBy(key, field, value);
    }

    public Double hincrByFloat(String key, String field, double value) {
	Jedis j = getShard(key);
	return j.hincrByFloat(key, field, value);
    }

    public Boolean hexists(String key, String field) {
	Jedis j = getShard(key);
	return j.hexists(key, field);
    }

    public Long del(String key) {
	Jedis j = getShard(key);
	return j.del(key);
    }

    public Long hdel(String key, String... fields) {
	Jedis j = getShard(key);
	return j.hdel(key, fields);
    }

    public Long hlen(String key) {
	Jedis j = getShard(key);
	return j.hlen(key);
    }

    public Set<String> hkeys(String key) {
	Jedis j = getShard(key);
	return j.hkeys(key);
    }

    public List<String> hvals(String key) {
	Jedis j = getShard(key);
	return j.hvals(key);
    }

    public Map<String, String> hgetAll(String key) {
	Jedis j = getShard(key);
	return j.hgetAll(key);
    }

    public Long rpush(String key, String... strings) {
	Jedis j = getShard(key);
	return j.rpush(key, strings);
    }

    public Long lpush(String key, String... strings) {
	Jedis j = getShard(key);
	return j.lpush(key, strings);
    }

    public Long lpushx(String key, String... string) {
	Jedis j = getShard(key);
	return j.lpushx(key, string);
    }

    public Long strlen(final String key) {
	Jedis j = getShard(key);
	return j.strlen(key);
    }

    public Long move(String key, int dbIndex) {
	Jedis j = getShard(key);
	return j.move(key, dbIndex);
    }

    public Long rpushx(String key, String... string) {
	Jedis j = getShard(key);
	return j.rpushx(key, string);
    }

    public Long persist(final String key) {
	Jedis j = getShard(key);
	return j.persist(key);
    }

    public Long llen(String key) {
	Jedis j = getShard(key);
	return j.llen(key);
    }

    public List<String> lrange(String key, long start, long end) {
	Jedis j = getShard(key);
	return j.lrange(key, start, end);
    }

    public String ltrim(String key, long start, long end) {
	Jedis j = getShard(key);
	return j.ltrim(key, start, end);
    }

    public String lindex(String key, long index) {
	Jedis j = getShard(key);
	return j.lindex(key, index);
    }

    public String lset(String key, long index, String value) {
	Jedis j = getShard(key);
	return j.lset(key, index, value);
    }

    public Long lrem(String key, long count, String value) {
	Jedis j = getShard(key);
	return j.lrem(key, count, value);
    }

    public String lpop(String key) {
	Jedis j = getShard(key);
	return j.lpop(key);
    }

    public String rpop(String key) {
	Jedis j = getShard(key);
	return j.rpop(key);
    }

    public Long sadd(String key, String... members) {
	Jedis j = getShard(key);
	return j.sadd(key, members);
    }

    public Set<String> smembers(String key) {
	Jedis j = getShard(key);
	return j.smembers(key);
    }

    public Long srem(String key, String... members) {
	Jedis j = getShard(key);
	return j.srem(key, members);
    }

    public String spop(String key) {
	Jedis j = getShard(key);
	return j.spop(key);
    }

    public Long scard(String key) {
	Jedis j = getShard(key);
	return j.scard(key);
    }

    public Boolean sismember(String key, String member) {
	Jedis j = getShard(key);
	return j.sismember(key, member);
    }

    public String srandmember(String key) {
	Jedis j = getShard(key);
	return j.srandmember(key);
    }

    public Long zadd(String key, double score, String member) {
	Jedis j = getShard(key);
	return j.zadd(key, score, member);
    }

    public Long zadd(String key, Map<String, Double> scoreMembers) {
	Jedis j = getShard(key);
	return j.zadd(key, scoreMembers);
    }

    public Set<String> zrange(String key, long start, long end) {
	Jedis j = getShard(key);
	return j.zrange(key, start, end);
    }

    public Long zrem(String key, String... members) {
	Jedis j = getShard(key);
	return j.zrem(key, members);
    }

    public Double zincrby(String key, double score, String member) {
	Jedis j = getShard(key);
	return j.zincrby(key, score, member);
    }

    public Long zrank(String key, String member) {
	Jedis j = getShard(key);
	return j.zrank(key, member);
    }

    public Long zrevrank(String key, String member) {
	Jedis j = getShard(key);
	return j.zrevrank(key, member);
    }

    public Set<String> zrevrange(String key, long start, long end) {
	Jedis j = getShard(key);
	return j.zrevrange(key, start, end);
    }

    public Set<Tuple> zrangeWithScores(String key, long start, long end) {
	Jedis j = getShard(key);
	return j.zrangeWithScores(key, start, end);
    }

    public Set<Tuple> zrevrangeWithScores(String key, long start, long end) {
	Jedis j = getShard(key);
	return j.zrevrangeWithScores(key, start, end);
    }

    public Long zcard(String key) {
	Jedis j = getShard(key);
	return j.zcard(key);
    }

    public Double zscore(String key, String member) {
	Jedis j = getShard(key);
	return j.zscore(key, member);
    }

    public List<String> sort(String key) {
	Jedis j = getShard(key);
	return j.sort(key);
    }

    public List<String> sort(String key, SortingParams sortingParameters) {
	Jedis j = getShard(key);
	return j.sort(key, sortingParameters);
    }

    public Long zcount(String key, double min, double max) {
	Jedis j = getShard(key);
	return j.zcount(key, min, max);
    }

    public Long zcount(String key, String min, String max) {
	Jedis j = getShard(key);
	return j.zcount(key, min, max);
    }

    public Set<String> zrangeByScore(String key, double min, double max) {
	Jedis j = getShard(key);
	return j.zrangeByScore(key, min, max);
    }

    public Set<String> zrevrangeByScore(String key, double max, double min) {
	Jedis j = getShard(key);
	return j.zrevrangeByScore(key, max, min);
    }

    public Set<String> zrangeByScore(String key, double min, double max,
	    int offset, int count) {
	Jedis j = getShard(key);
	return j.zrangeByScore(key, min, max, offset, count);
    }

    public Set<String> zrevrangeByScore(String key, double max, double min,
	    int offset, int count) {
	Jedis j = getShard(key);
	return j.zrevrangeByScore(key, max, min, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min, double max) {
	Jedis j = getShard(key);
	return j.zrangeByScoreWithScores(key, min, max);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
	    double min) {
	Jedis j = getShard(key);
	return j.zrevrangeByScoreWithScores(key, max, min);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, double min,
	    double max, int offset, int count) {
	Jedis j = getShard(key);
	return j.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, double max,
	    double min, int offset, int count) {
	Jedis j = getShard(key);
	return j.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Set<String> zrangeByScore(String key, String min, String max) {
	Jedis j = getShard(key);
	return j.zrangeByScore(key, min, max);
    }

    public Set<String> zrevrangeByScore(String key, String max, String min) {
	Jedis j = getShard(key);
	return j.zrevrangeByScore(key, max, min);
    }

    public Set<String> zrangeByScore(String key, String min, String max,
	    int offset, int count) {
	Jedis j = getShard(key);
	return j.zrangeByScore(key, min, max, offset, count);
    }

    public Set<String> zrevrangeByScore(String key, String max, String min,
	    int offset, int count) {
	Jedis j = getShard(key);
	return j.zrevrangeByScore(key, max, min, offset, count);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, String min, String max) {
	Jedis j = getShard(key);
	return j.zrangeByScoreWithScores(key, min, max);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
	    String min) {
	Jedis j = getShard(key);
	return j.zrevrangeByScoreWithScores(key, max, min);
    }

    public Set<Tuple> zrangeByScoreWithScores(String key, String min,
	    String max, int offset, int count) {
	Jedis j = getShard(key);
	return j.zrangeByScoreWithScores(key, min, max, offset, count);
    }

    public Set<Tuple> zrevrangeByScoreWithScores(String key, String max,
	    String min, int offset, int count) {
	Jedis j = getShard(key);
	return j.zrevrangeByScoreWithScores(key, max, min, offset, count);
    }

    public Long zremrangeByRank(String key, long start, long end) {
	Jedis j = getShard(key);
	return j.zremrangeByRank(key, start, end);
    }

    public Long zremrangeByScore(String key, double start, double end) {
	Jedis j = getShard(key);
	return j.zremrangeByScore(key, start, end);
    }

    public Long zremrangeByScore(String key, String start, String end) {
	Jedis j = getShard(key);
	return j.zremrangeByScore(key, start, end);
    }

    public Long linsert(String key, LIST_POSITION where, String pivot,
	    String value) {
	Jedis j = getShard(key);
	return j.linsert(key, where, pivot, value);
    }

    public Long bitcount(final String key) {
	Jedis j = getShard(key);
	return j.bitcount(key);
    }

    public Long bitcount(final String key, long start, long end) {
	Jedis j = getShard(key);
	return j.bitcount(key, start, end);
    }

    @Deprecated
    /**
     * This method is deprecated due to bug (scan cursor should be unsigned long)
     * And will be removed on next major release
     * @see https://github.com/xetorthio/jedis/issues/531 
     */
    public ScanResult<Entry<String, String>> hscan(String key, int cursor) {
	Jedis j = getShard(key);
	return j.hscan(key, cursor);
    }

    @Deprecated
    /**
     * This method is deprecated due to bug (scan cursor should be unsigned long)
     * And will be removed on next major release
     * @see https://github.com/xetorthio/jedis/issues/531 
     */
    public ScanResult<String> sscan(String key, int cursor) {
	Jedis j = getShard(key);
	return j.sscan(key, cursor);
    }

    @Deprecated
    /**
     * This method is deprecated due to bug (scan cursor should be unsigned long)
     * And will be removed on next major release
     * @see https://github.com/xetorthio/jedis/issues/531 
     */
    public ScanResult<Tuple> zscan(String key, int cursor) {
	Jedis j = getShard(key);
	return j.zscan(key, cursor);
    }

    public ScanResult<Entry<String, String>> hscan(String key,
	    final String cursor) {
	Jedis j = getShard(key);
	return j.hscan(key, cursor);
    }

    public ScanResult<String> sscan(String key, final String cursor) {
	Jedis j = getShard(key);
	return j.sscan(key, cursor);
    }

    public ScanResult<Tuple> zscan(String key, final String cursor) {
	Jedis j = getShard(key);
	return j.zscan(key, cursor);
    }

    @Override
    public void close() {
	if (dataSource != null) {
	    boolean broken = false;

	    for (Jedis jedis : getAllShards()) {
		if (jedis.getClient().isBroken()) {
		    broken = true;
		}
	    }

	    if (broken) {
		dataSource.returnBrokenResource(this);
	    } else {
		this.resetState();
		dataSource.returnResource(this);
	    }

	} else {
	    disconnect();
	}
    }

    public void setDataSource(Pool<ShardedJedis> shardedJedisPool) {
	this.dataSource = shardedJedisPool;
    }

    public void resetState() {
	for (Jedis jedis : getAllShards()) {
	    jedis.resetState();
	}
    }

    public Long pfadd(String key, String... elements) {
	Jedis j = getShard(key);
	return j.pfadd(key, elements);
    }

    @Override
    public long pfcount(String key) {
	Jedis j = getShard(key);
	return j.pfcount(key);
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

