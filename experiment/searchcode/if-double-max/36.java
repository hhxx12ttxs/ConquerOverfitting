<<<<<<< HEAD
package redis.clients.jedis;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.BinaryClient.LIST_POSITION;

public class Jedis extends BinaryJedis implements JedisCommands {
    public Jedis(final String host) {
        super(host);
    }

    public Jedis(final String host, final int port) {
        super(host, port);
    }

    public Jedis(final String host, final int port, final int timeout) {
        super(host, port, timeout);
    }

    public Jedis(JedisShardInfo shardInfo) {
        super(shardInfo);
    }

	@Override
    public String ping() {
        checkIsInMulti();
        client.ping();
        return client.getStatusCodeReply();
    }

    /**
	 * Resets the client.
	 * <p>
	 * Reads all remaining responses from the input.
	 */
	public void reset() {
		client.getAll();
	}

	/**
     * Set the string value as value of the key. The string can't be longer than
     * 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Status code reply
     */
    public String set(final String key, String value) {
        checkIsInMulti();
        client.set(key, value);
        return client.getStatusCodeReply();
    }

    /**
     * Get the value of the specified key. If the key does not exist the special
     * value 'nil' is returned. If the value stored at key is not a string an
     * error is returned because GET can only handle string values.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public String get(final String key) {
        checkIsInMulti();
        client.sendCommand(Protocol.Command.GET, key);
        return client.getBulkReply();
    }

    /**
     * Ask the server to silently close the connection.
     */

    @Override
    public String quit() {
        checkIsInMulti();
        client.quit();
        return client.getStatusCodeReply();
    }

    /**
     * Test if the specified key exists. The command returns "1" if the key
     * exists, otherwise "0" is returned. Note that even keys set with an empty
     * string as value will return "1".
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Boolean reply, true if the key exists, otherwise false
     */
    public Boolean exists(final String key) {
        checkIsInMulti();
        client.exists(key);
        return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified keys. If a given key does not exist no operation is
     * performed for this key. The command returns the number of keys removed.
     * 
     * Time complexity: O(1)
     * 
     * @param keys
     * @return Integer reply, specifically: an integer greater than 0 if one or
     *         more keys were removed 0 if none of the specified key existed
     */
    public Long del(final String... keys) {
        checkIsInMulti();
        client.del(keys);
        return client.getIntegerReply();
    }

    /**
     * Return the type of the value stored at key in form of a string. The type
     * can be one of "none", "string", "list", "set". "none" is returned if the
     * key does not exist.
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Status code reply, specifically: "none" if the key does not exist
     *         "string" if the key contains a String value "list" if the key
     *         contains a List value "set" if the key contains a Set value
     *         "zset" if the key contains a Sorted Set value "hash" if the key
     *         contains a Hash value
     */
    public String type(final String key) {
        checkIsInMulti();
        client.type(key);
        return client.getStatusCodeReply();
    }

    /**
     * Delete all the keys of the currently selected DB. This command never
     * fails.
     * 
     * @return Status code reply
     */

    @Override
    public String flushDB() {
        checkIsInMulti();
        client.flushDB();
        return client.getStatusCodeReply();
    }

    /**
     * Returns all the keys matching the glob-style pattern as space separated
     * strings. For example if you have in the database the keys "foo" and
     * "foobar" the command "KEYS foo*" will return "foo foobar".
     * <p>
     * Note that while the time complexity for this operation is O(n) the
     * constant times are pretty low. For example Redis running on an entry
     * level laptop can scan a 1 million keys database in 40 milliseconds.
     * <b>Still it's better to consider this one of the slow commands that may
     * ruin the DB performance if not used with care.</b>
     * <p>
     * In other words this command is intended only for debugging and special
     * operations like creating a script to change the DB schema. Don't use it
     * in your normal code. Use Redis Sets in order to group together a subset
     * of objects.
     * <p>
     * Glob style patterns examples:
     * <ul>
     * <li>h?llo will match hello hallo hhllo
     * <li>h*llo will match hllo heeeello
     * <li>h[ae]llo will match hello and hallo, but not hillo
     * </ul>
     * <p>
     * Use \ to escape special chars if you want to match them verbatim.
     * <p>
     * Time complexity: O(n) (with n being the number of keys in the DB, and
     * assuming keys and pattern of limited length)
     * 
     * @param pattern
     * @return Multi bulk reply
     */
    public Set<String> keys(final String pattern) {
        checkIsInMulti();
        client.keys(pattern);
        return BuilderFactory.STRING_SET
                .build(client.getBinaryMultiBulkReply());
    }

    /**
     * Return a randomly selected key from the currently selected DB.
     * <p>
     * Time complexity: O(1)
     * 
     * @return Singe line reply, specifically the randomly selected key or an
     *         empty string is the database is empty
     */
    public String randomKey() {
        checkIsInMulti();
        client.randomKey();
        return client.getBulkReply();
    }

    /**
     * Atomically renames the key oldkey to newkey. If the source and
     * destination name are the same an error is returned. If newkey already
     * exists it is overwritten.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Status code repy
     */
    public String rename(final String oldkey, final String newkey) {
        checkIsInMulti();
        client.rename(oldkey, newkey);
        return client.getStatusCodeReply();
    }

    /**
     * Rename oldkey into newkey but fails if the destination key newkey already
     * exists.
     * <p>
     * Time complexity: O(1)
     * 
     * @param oldkey
     * @param newkey
     * @return Integer reply, specifically: 1 if the key was renamed 0 if the
     *         target key already exist
     */
    public Long renamenx(final String oldkey, final String newkey) {
        checkIsInMulti();
        client.renamenx(oldkey, newkey);
        return client.getIntegerReply();
    }

    /**
     * Return the number of keys in the currently selected database.
     * 
     * @return Integer reply
     */

    @Override
    public Long dbSize() {
        checkIsInMulti();
        client.dbSize();
        return client.getIntegerReply();
    }

    /**
     * Set a timeout on the specified key. After the timeout the key will be
     * automatically deleted by the server. A key with an associated timeout is
     * said to be volatile in Redis terminology.
     * <p>
     * Voltile keys are stored on disk like the other keys, the timeout is
     * persistent too like all the other aspects of the dataset. Saving a
     * dataset containing expires and stopping the server does not stop the flow
     * of time as Redis stores on disk the time when the key will no longer be
     * available as Unix time, and not the remaining seconds.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(String)
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param seconds
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expire(final String key, final int seconds) {
        checkIsInMulti();
        client.expire(key, seconds);
        return client.getIntegerReply();
    }

    /**
     * EXPIREAT works exctly like {@link #expire(String, int) EXPIRE} but
     * instead to get the number of seconds representing the Time To Live of the
     * key as a second argument (that is a relative way of specifing the TTL),
     * it takes an absolute one in the form of a UNIX timestamp (Number of
     * seconds elapsed since 1 Gen 1970).
     * <p>
     * EXPIREAT was introduced in order to implement the Append Only File
     * persistence mode so that EXPIRE commands are automatically translated
     * into EXPIREAT commands for the append only file. Of course EXPIREAT can
     * also used by programmers that need a way to simply specify that a given
     * key should expire at a given time in the future.
     * <p>
     * Since Redis 2.1.3 you can update the value of the timeout of a key
     * already having an expire set. It is also possible to undo the expire at
     * all turning the key into a normal key using the {@link #persist(String)
     * PERSIST} command.
     * <p>
     * Time complexity: O(1)
     * 
     * @see <ahref="http://code.google.com/p/redis/wiki/ExpireCommand">ExpireCommand</a>
     * 
     * @param key
     * @param unixTime
     * @return Integer reply, specifically: 1: the timeout was set. 0: the
     *         timeout was not set since the key already has an associated
     *         timeout (this may happen only in Redis versions < 2.1.3, Redis >=
     *         2.1.3 will happily update the timeout), or the key does not
     *         exist.
     */
    public Long expireAt(final String key, final long unixTime) {
        checkIsInMulti();
        client.expireAt(key, unixTime);
        return client.getIntegerReply();
    }

    /**
     * The TTL command returns the remaining time to live in seconds of a key
     * that has an {@link #expire(String, int) EXPIRE} set. This introspection
     * capability allows a Redis client to check how many seconds a given key
     * will continue to be part of the dataset.
     * 
     * @param key
     * @return Integer reply, returns the remaining time to live in seconds of a
     *         key that has an EXPIRE. If the Key does not exists or does not
     *         have an associated expire, -1 is returned.
     */
    public Long ttl(final String key) {
        checkIsInMulti();
        client.ttl(key);
        return client.getIntegerReply();
    }

    /**
     * Select the DB with having the specified zero-based numeric index. For
     * default every new client connection is automatically selected to DB 0.
     * 
     * @param index
     * @return Status code reply
     */

    @Override
    public String select(final int index) {
        checkIsInMulti();
        client.select(index);
        return client.getStatusCodeReply();
    }

    /**
     * Move the specified key from the currently selected DB to the specified
     * destination DB. Note that this command returns 1 only if the key was
     * successfully moved, and 0 if the target key was already there or if the
     * source key was not found at all, so it is possible to use MOVE as a
     * locking primitive.
     * 
     * @param key
     * @param dbIndex
     * @return Integer reply, specifically: 1 if the key was moved 0 if the key
     *         was not moved because already present on the target DB or was not
     *         found in the current DB.
     */
    public Long move(final String key, final int dbIndex) {
        checkIsInMulti();
        client.move(key, dbIndex);
        return client.getIntegerReply();
    }

    /**
     * Delete all the keys of all the existing databases, not just the currently
     * selected one. This command never fails.
     * 
     * @return Status code reply
     */

    @Override
    public String flushAll() {
        checkIsInMulti();
        client.flushAll();
        return client.getStatusCodeReply();
    }

    /**
     * GETSET is an atomic set this value and return the old value command. Set
     * key to the string value and return the old value stored at key. The
     * string can't be longer than 1073741824 bytes (1 GB).
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Bulk reply
     */
    public String getSet(final String key, final String value) {
        checkIsInMulti();
        client.getSet(key, value);
        return client.getBulkReply();
    }

    /**
     * Get the values of all the specified keys. If one or more keys dont exist
     * or is not of type String, a 'nil' value is returned instead of the value
     * of the specified key, but the operation never fails.
     * <p>
     * Time complexity: O(1) for every key
     * 
     * @param keys
     * @return Multi bulk reply
     */
    public List<String> mget(final String... keys) {
        checkIsInMulti();
        client.mget(keys);
        return client.getMultiBulkReply();
    }

    /**
     * SETNX works exactly like {@link #set(String, String) SET} with the only
     * difference that if the key already exists no operation is performed.
     * SETNX actually means "SET if Not eXists".
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically: 1 if the key was set 0 if the key
     *         was not set
     */
    public Long setnx(final String key, final String value) {
        checkIsInMulti();
        client.setnx(key, value);
        return client.getIntegerReply();
    }

    /**
     * The command is exactly equivalent to the following group of commands:
     * {@link #set(String, String) SET} + {@link #expire(String, int) EXPIRE}.
     * The operation is atomic.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @param seconds
     * @param value
     * @return Status code reply
     */
    public String setex(final String key, final int seconds, final String value) {
        checkIsInMulti();
        client.setex(key, seconds, value);
        return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values. MSET will replace
     * old values with new values, while {@link #msetnx(String...) MSETNX} will
     * not perform any operation at all even if just a single key already
     * exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #msetnx(String...)
     * 
     * @param keysvalues
     * @return Status code reply Basically +OK as MSET can't fail
     */
    public String mset(final String... keysvalues) {
        checkIsInMulti();
        client.mset(keysvalues);
        return client.getStatusCodeReply();
    }

    /**
     * Set the the respective keys to the respective values.
     * {@link #mset(String...) MSET} will replace old values with new values,
     * while MSETNX will not perform any operation at all even if just a single
     * key already exists.
     * <p>
     * Because of this semantic MSETNX can be used in order to set different
     * keys representing different fields of an unique logic object in a way
     * that ensures that either all the fields or none at all are set.
     * <p>
     * Both MSET and MSETNX are atomic operations. This means that for instance
     * if the keys A and B are modified, another client talking to Redis can
     * either see the changes to both A and B at once, or no modification at
     * all.
     * 
     * @see #mset(String...)
     * 
     * @param keysvalues
     * @return Integer reply, specifically: 1 if the all the keys were set 0 if
     *         no key was set (at least one key already existed)
     */
    public Long msetnx(final String... keysvalues) {
        checkIsInMulti();
        client.msetnx(keysvalues);
        return client.getIntegerReply();
    }

    /**
     * IDECRBY work just like {@link #decr(String) INCR} but instead to
     * decrement by 1 the decrement is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #decr(String)
     * @see #incrBy(String, long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decrBy(final String key, final long integer) {
        checkIsInMulti();
        client.decrBy(key, integer);
        return client.getIntegerReply();
    }

    /**
     * Decrement the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the decrement operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #incrBy(String, long)
     * @see #decrBy(String, long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long decr(final String key) {
        checkIsInMulti();
        client.decr(key);
        return client.getIntegerReply();
    }

    /**
     * INCRBY work just like {@link #incr(String) INCR} but instead to increment
     * by 1 the increment is integer.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incr(String)
     * @see #decr(String)
     * @see #decrBy(String, long)
     * 
     * @param key
     * @param integer
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incrBy(final String key, final long integer) {
        checkIsInMulti();
        client.incrBy(key, integer);
        return client.getIntegerReply();
    }

    /**
     * Increment the number stored at key by one. If the key does not exist or
     * contains a value of a wrong type, set the key to the value of "0" before
     * to perform the increment operation.
     * <p>
     * INCR commands are limited to 64 bit signed integers.
     * <p>
     * Note: this is actually a string operation, that is, in Redis there are
     * not "integer" types. Simply the string stored at the key is parsed as a
     * base 10 64 bit signed integer, incremented, and then converted back as a
     * string.
     * <p>
     * Time complexity: O(1)
     * 
     * @see #incrBy(String, long)
     * @see #decr(String)
     * @see #decrBy(String, long)
     * 
     * @param key
     * @return Integer reply, this commands will reply with the new value of key
     *         after the increment.
     */
    public Long incr(final String key) {
        checkIsInMulti();
        client.incr(key);
        return client.getIntegerReply();
    }

    /**
     * If the key already exists and is a string, this command appends the
     * provided value at the end of the string. If the key does not exist it is
     * created and set as an empty string, so APPEND will be very similar to SET
     * in this special case.
     * <p>
     * Time complexity: O(1). The amortized time complexity is O(1) assuming the
     * appended value is small and the already present value is of any size,
     * since the dynamic string library used by Redis will double the free space
     * available on every reallocation.
     * 
     * @param key
     * @param value
     * @return Integer reply, specifically the total length of the string after
     *         the append operation.
     */
    public Long append(final String key, final String value) {
        checkIsInMulti();
        client.append(key, value);
        return client.getIntegerReply();
    }

    /**
     * Return a subset of the string from offset start to offset end (both
     * offsets are inclusive). Negative offsets can be used in order to provide
     * an offset starting from the end of the string. So -1 means the last char,
     * -2 the penultimate and so forth.
     * <p>
     * The function handles out of range requests without raising an error, but
     * just limiting the resulting range to the actual length of the string.
     * <p>
     * Time complexity: O(start+n) (with start being the start index and n the
     * total length of the requested range). Note that the lookup part of this
     * command is O(1) so for small strings this is actually an O(1) command.
     * 
     * @param key
     * @param start
     * @param end
     * @return Bulk reply
     */
    public String substr(final String key, final int start, final int end) {
        checkIsInMulti();
        client.substr(key, start, end);
        return client.getBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, and the HSET just produced an update
     *         of the value, 0 is returned, otherwise if a new field is created
     *         1 is returned.
     */
    public Long hset(final String key, final String field, final String value) {
        checkIsInMulti();
        client.hset(key, field, value);
        return client.getIntegerReply();
    }

    /**
     * If key holds a hash, retrieve the value associated to the specified
     * field.
     * <p>
     * If the field is not found or the key does not exist, a special 'nil'
     * value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Bulk reply
     */
    public String hget(final String key, final String field) {
        checkIsInMulti();
        client.hget(key, field);
        return client.getBulkReply();
    }

    /**
     * 
     * Set the specified hash field to the specified value if the field not
     * exists. <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return If the field already exists, 0 is returned, otherwise if a new
     *         field is created 1 is returned.
     */
    public Long hsetnx(final String key, final String field, final String value) {
        checkIsInMulti();
        client.hsetnx(key, field, value);
        return client.getIntegerReply();
    }

    /**
     * Set the respective fields to the respective values. HMSET replaces old
     * values with new values.
     * <p>
     * If key does not exist, a new key holding a hash is created.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param hash
     * @return Return OK or Exception if hash is empty
     */
    public String hmset(final String key, final Map<String, String> hash) {
        checkIsInMulti();
        client.hmset(key, hash);
        return client.getStatusCodeReply();
    }

    /**
     * Retrieve the values associated to the specified fields.
     * <p>
     * If some of the specified fields do not exist, nil values are returned.
     * Non existing keys are considered like empty hashes.
     * <p>
     * <b>Time complexity:</b> O(N) (with N being the number of fields)
     * 
     * @param key
     * @param fields
     * @return Multi Bulk Reply specifically a list of all the values associated
     *         with the specified fields, in the same order of the request.
     */
    public List<String> hmget(final String key, final String... fields) {
        checkIsInMulti();
        client.hmget(key, fields);
        return client.getMultiBulkReply();
    }

    /**
     * Increment the number stored at field in the hash at key by value. If key
     * does not exist, a new key holding a hash is created. If field does not
     * exist or holds a string, the value is set to 0 before applying the
     * operation. Since the value argument is signed you can use this command to
     * perform both increments and decrements.
     * <p>
     * The range of values supported by HINCRBY is limited to 64 bit signed
     * integers.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @param value
     * @return Integer reply The new value at field after the increment
     *         operation.
     */
    public Long hincrBy(final String key, final String field, final long value) {
        checkIsInMulti();
        client.hincrBy(key, field, value);
        return client.getIntegerReply();
    }

    /**
     * Test for existence of a specified field in a hash.
     * 
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return Return 1 if the hash stored at key contains the specified field.
     *         Return 0 if the key is not found or the field is not present.
     */
    public Boolean hexists(final String key, final String field) {
        checkIsInMulti();
        client.hexists(key, field);
        return client.getIntegerReply() == 1;
    }

    /**
     * Remove the specified field from an hash stored at key.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param field
     * @return If the field was present in the hash it is deleted and 1 is
     *         returned, otherwise 0 is returned and no operation is performed.
     */
    public Long hdel(final String key, final String field) {
        checkIsInMulti();
        client.hdel(key, field);
        return client.getIntegerReply();
    }

    /**
     * Return the number of items in a hash.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @return The number of entries (fields) contained in the hash stored at
     *         key. If the specified key does not exist, 0 is returned assuming
     *         an empty hash.
     */
    public Long hlen(final String key) {
        checkIsInMulti();
        client.hlen(key);
        return client.getIntegerReply();
    }

    /**
     * Return all the fields in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields names contained into a hash.
     */
    public Set<String> hkeys(final String key) {
        checkIsInMulti();
        client.hkeys(key);
        return BuilderFactory.STRING_SET
                .build(client.getBinaryMultiBulkReply());
    }

    /**
     * Return all the values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields values contained into a hash.
     */
    public List<String> hvals(final String key) {
        checkIsInMulti();
        client.hvals(key);
        final List<String> lresult = client.getMultiBulkReply();
        return lresult;
    }

    /**
     * Return all the fields and associated values in a hash.
     * <p>
     * <b>Time complexity:</b> O(N), where N is the total number of entries
     * 
     * @param key
     * @return All the fields and values contained into a hash.
     */
    public Map<String, String> hgetAll(final String key) {
        checkIsInMulti();
        client.hgetAll(key);
        return BuilderFactory.STRING_MAP
                .build(client.getBinaryMultiBulkReply());
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see Jedis#lpush(String, String)
     * 
     * @param key
     * @param string
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long rpush(final String key, final String string) {
        checkIsInMulti();
        client.rpush(key, string);
        return client.getIntegerReply();
    }

    /**
     * Add the string value to the head (LPUSH) or tail (RPUSH) of the list
     * stored at key. If the key does not exist an empty list is created just
     * before the append operation. If the key exists but is not a List an error
     * is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @see Jedis#rpush(String, String)
     * 
     * @param key
     * @param string
     * @return Integer reply, specifically, the number of elements inside the
     *         list after the push operation.
     */
    public Long lpush(final String key, final String string) {
        checkIsInMulti();
        client.lpush(key, string);
        return client.getIntegerReply();
    }

    /**
     * Return the length of the list stored at the specified key. If the key
     * does not exist zero is returned (the same behaviour as for empty lists).
     * If the value stored at key is not a list an error is returned.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return The length of the list.
     */
    public Long llen(final String key) {
        checkIsInMulti();
        client.llen(key);
        return client.getIntegerReply();
    }

    /**
     * Return the specified elements of the list stored at the specified key.
     * Start and end are zero-based indexes. 0 is the first element of the list
     * (the list head), 1 the next element and so on.
     * <p>
     * For example LRANGE foobar 0 2 will return the first three elements of the
     * list.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * <b>Consistency with range functions in various programming languages</b>
     * <p>
     * Note that if you have a list of numbers from 0 to 100, LRANGE 0 10 will
     * return 11 elements, that is, rightmost item is included. This may or may
     * not be consistent with behavior of range-related functions in your
     * programming language of choice (think Ruby's Range.new, Array#slice or
     * Python's range() function).
     * <p>
     * LRANGE behavior is consistent with one of Tcl.
     * <p>
     * <b>Out-of-range indexes</b>
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is returned. If end is over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Time complexity: O(start+n) (with n being the length of the range and
     * start being the start offset)
     * 
     * @param key
     * @param start
     * @param end
     * @return Multi bulk reply, specifically a list of elements in the
     *         specified range.
     */
    public List<String> lrange(final String key, final long start, final long end) {
        checkIsInMulti();
        client.lrange(key, start, end);
        return client.getMultiBulkReply();
    }

    /**
     * Trim an existing list so that it will contain only the specified range of
     * elements specified. Start and end are zero-based indexes. 0 is the first
     * element of the list (the list head), 1 the next element and so on.
     * <p>
     * For example LTRIM foobar 0 2 will modify the list stored at foobar key so
     * that only the first three elements of the list will remain.
     * <p>
     * start and end can also be negative numbers indicating offsets from the
     * end of the list. For example -1 is the last element of the list, -2 the
     * penultimate element and so on.
     * <p>
     * Indexes out of range will not produce an error: if start is over the end
     * of the list, or start > end, an empty list is left as value. If end over
     * the end of the list Redis will threat it just like the last element of
     * the list.
     * <p>
     * Hint: the obvious use of LTRIM is together with LPUSH/RPUSH. For example:
     * <p>
     * {@code lpush("mylist", "someelement"); ltrim("mylist", 0, 99); * }
     * <p>
     * The above two commands will push elements in the list taking care that
     * the list will not grow without limits. This is very useful when using
     * Redis to store logs for example. It is important to note that when used
     * in this way LTRIM is an O(1) operation because in the average case just
     * one element is removed from the tail of the list.
     * <p>
     * Time complexity: O(n) (with n being len of list - len of range)
     * 
     * @param key
     * @param start
     * @param end
     * @return Status code reply
     */
    public String ltrim(final String key, final long start, final long end) {
        checkIsInMulti();
        client.ltrim(key, start, end);
        return client.getStatusCodeReply();
    }

    /**
     * Return the specified element of the list stored at the specified key. 0
     * is the first element, 1 the second and so on. Negative indexes are
     * supported, for example -1 is the last element, -2 the penultimate and so
     * on.
     * <p>
     * If the value stored at key is not of list type an error is returned. If
     * the index is out of range a 'nil' reply is returned.
     * <p>
     * Note that even if the average time complexity is O(n) asking for the
     * first or the last element of the list is O(1).
     * <p>
     * Time complexity: O(n) (with n being the length of the list)
     * 
     * @param key
     * @param index
     * @return Bulk reply, specifically the requested element
     */
    public String lindex(final String key, final long index) {
        checkIsInMulti();
        client.lindex(key, index);
        return client.getBulkReply();
    }

    /**
     * Set a new value as the element at index position of the List at key.
     * <p>
     * Out of range indexes will generate an error.
     * <p>
     * Similarly to other list commands accepting indexes, the index can be
     * negative to access elements starting from the end of the list. So -1 is
     * the last element, -2 is the penultimate, and so forth.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) (with N being the length of the list), setting the first or last
     * elements of the list is O(1).
     * 
     * @see #lindex(String, long)
     * 
     * @param key
     * @param index
     * @param value
     * @return Status code reply
     */
    public String lset(final String key, final long index, final String value) {
        checkIsInMulti();
        client.lset(key, index, value);
        return client.getStatusCodeReply();
    }

    /**
     * Remove the first count occurrences of the value element from the list. If
     * count is zero all the elements are removed. If count is negative elements
     * are removed from tail to head, instead to go from head to tail that is
     * the normal behaviour. So for example LREM with count -2 and hello as
     * value to remove against the list (a,b,c,hello,x,hello,hello) will lave
     * the list (a,b,c,hello,x). The number of removed elements is returned as
     * an integer, see below for more information about the returned value. Note
     * that non existing keys are considered like empty lists by LREM, so LREM
     * against non existing keys will always return 0.
     * <p>
     * Time complexity: O(N) (with N being the length of the list)
     * 
     * @param key
     * @param count
     * @param value
     * @return Integer Reply, specifically: The number of removed elements if
     *         the operation succeeded
     */
    public Long lrem(final String key, final long count, final String value) {
        checkIsInMulti();
        client.lrem(key, count, value);
        return client.getIntegerReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #rpop(String)
     * 
     * @param key
     * @return Bulk reply
     */
    public String lpop(final String key) {
        checkIsInMulti();
        client.lpop(key);
        return client.getBulkReply();
    }

    /**
     * Atomically return and remove the first (LPOP) or last (RPOP) element of
     * the list. For example if the list contains the elements "a","b","c" LPOP
     * will return "a" and the list will become "b","c".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned.
     * 
     * @see #lpop(String)
     * 
     * @param key
     * @return Bulk reply
     */
    public String rpop(final String key) {
        checkIsInMulti();
        client.rpop(key);
        return client.getBulkReply();
    }

    /**
     * Atomically return and remove the last (tail) element of the srckey list,
     * and push the element as the first (head) element of the dstkey list. For
     * example if the source list contains the elements "a","b","c" and the
     * destination list contains the elements "foo","bar" after an RPOPLPUSH
     * command the content of the two lists will be "a","b" and "c","foo","bar".
     * <p>
     * If the key does not exist or the list is already empty the special value
     * 'nil' is returned. If the srckey and dstkey are the same the operation is
     * equivalent to removing the last element from the list and pusing it as
     * first element of the list, so it's a "list rotation" command.
     * <p>
     * Time complexity: O(1)
     * 
     * @param srckey
     * @param dstkey
     * @return Bulk reply
     */
    public String rpoplpush(final String srckey, final String dstkey) {
        checkIsInMulti();
        client.rpoplpush(srckey, dstkey);
        return client.getBulkReply();
    }

    /**
     * Add the specified member to the set value stored at key. If member is
     * already a member of the set no operation is performed. If key does not
     * exist a new set with the specified member as sole member is created. If
     * the key exists but does not hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the set
     */
    public Long sadd(final String key, final String member) {
        checkIsInMulti();
        client.sadd(key, member);
        return client.getIntegerReply();
    }

    /**
     * Return all the members (elements) of the set value stored at key. This is
     * just syntax glue for {@link #sinter(String...) SINTER}.
     * <p>
     * Time complexity O(N)
     * 
     * @param key
     * @return Multi bulk reply
     */
    public Set<String> smembers(final String key) {
        checkIsInMulti();
        client.smembers(key);
        final List<String> members = client.getMultiBulkReply();
        return new HashSet<String>(members);
    }

    /**
     * Remove the specified member from the set value stored at key. If member
     * was not a member of the set no operation is performed. If key does not
     * hold a set value an error is returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long srem(final String key, final String member) {
        checkIsInMulti();
        client.srem(key, member);
        return client.getIntegerReply();
    }

    /**
     * Remove a random element from a Set returning it as return value. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The {@link #srandmember(String)} command does a similar work but the
     * returned element is not removed from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public String spop(final String key) {
        checkIsInMulti();
        client.spop(key);
        return client.getBulkReply();
    }

    /**
     * Move the specifided member from the set at srckey to the set at dstkey.
     * This operation is atomic, in every given moment the element will appear
     * to be in the source or destination set for accessing clients.
     * <p>
     * If the source set does not exist or does not contain the specified
     * element no operation is performed and zero is returned, otherwise the
     * element is removed from the source set and added to the destination set.
     * On success one is returned, even if the element was already present in
     * the destination set.
     * <p>
     * An error is raised if the source or destination keys contain a non Set
     * value.
     * <p>
     * Time complexity O(1)
     * 
     * @param srckey
     * @param dstkey
     * @param member
     * @return Integer reply, specifically: 1 if the element was moved 0 if the
     *         element was not found on the first set and no operation was
     *         performed
     */
    public Long smove(final String srckey, final String dstkey,
            final String member) {
        checkIsInMulti();
        client.smove(srckey, dstkey, member);
        return client.getIntegerReply();
    }

    /**
     * Return the set cardinality (number of elements). If the key does not
     * exist 0 is returned, like for empty sets.
     * 
     * @param key
     * @return Integer reply, specifically: the cardinality (number of elements)
     *         of the set as an integer.
     */
    public Long scard(final String key) {
        checkIsInMulti();
        client.scard(key);
        return client.getIntegerReply();
    }

    /**
     * Return 1 if member is a member of the set stored at key, otherwise 0 is
     * returned.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the element is a member of the
     *         set 0 if the element is not a member of the set OR if the key
     *         does not exist
     */
    public Boolean sismember(final String key, final String member) {
        checkIsInMulti();
        client.sismember(key, member);
        return client.getIntegerReply() == 1;
    }

    /**
     * Return the members of a set resulting from the intersection of all the
     * sets hold at the specified keys. Like in
     * {@link #lrange(String, long, long) LRANGE} the result is sent to the client
     * as a multi-bulk reply (see the protocol specification for more
     * information). If just a single key is specified, then this command
     * produces the same result as {@link #smembers(String) SMEMBERS}. Actually
     * SMEMBERS is just syntax sugar for SINTER.
     * <p>
     * Non existing keys are considered like empty sets, so if one of the keys
     * is missing an empty set is returned (since the intersection with an empty
     * set always is an empty set).
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<String> sinter(final String... keys) {
        checkIsInMulti();
        client.sinter(keys);
        final List<String> members = client.getMultiBulkReply();
        return new HashSet<String>(members);
    }

    /**
     * This commnad works exactly like {@link #sinter(String...) SINTER} but
     * instead of being returned the resulting set is sotred as dstkey.
     * <p>
     * Time complexity O(N*M) worst case where N is the cardinality of the
     * smallest set and M the number of sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sinterstore(final String dstkey, final String... keys) {
        checkIsInMulti();
        client.sinterstore(dstkey, keys);
        return client.getIntegerReply();
    }

    /**
     * Return the members of a set resulting from the union of all the sets hold
     * at the specified keys. Like in {@link #lrange(String, long, long) LRANGE}
     * the result is sent to the client as a multi-bulk reply (see the protocol
     * specification for more information). If just a single key is specified,
     * then this command produces the same result as {@link #smembers(String)
     * SMEMBERS}.
     * <p>
     * Non existing keys are considered like empty sets.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param keys
     * @return Multi bulk reply, specifically the list of common elements.
     */
    public Set<String> sunion(final String... keys) {
        checkIsInMulti();
        client.sunion(keys);
        final List<String> members = client.getMultiBulkReply();
        return new HashSet<String>(members);
    }

    /**
     * This command works exactly like {@link #sunion(String...) SUNION} but
     * instead of being returned the resulting set is stored as dstkey. Any
     * existing value in dstkey will be over-written.
     * <p>
     * Time complexity O(N) where N is the total number of elements in all the
     * provided sets
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sunionstore(final String dstkey, final String... keys) {
        checkIsInMulti();
        client.sunionstore(dstkey, keys);
        return client.getIntegerReply();
    }

    /**
     * Return the difference between the Set stored at key1 and all the Sets
     * key2, ..., keyN
     * <p>
     * <b>Example:</b>
     * 
     * <pre>
     * key1 = [x, a, b, c]
     * key2 = [c]
     * key3 = [a, d]
     * SDIFF key1,key2,key3 => [x, b]
     * </pre>
     * 
     * Non existing keys are considered like empty sets.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(N) with N being the total number of elements of all the sets
     * 
     * @param keys
     * @return Return the members of a set resulting from the difference between
     *         the first set provided and all the successive sets.
     */
    public Set<String> sdiff(final String... keys) {
        checkIsInMulti();
        client.sdiff(keys);
        return BuilderFactory.STRING_SET
                .build(client.getBinaryMultiBulkReply());
    }

    /**
     * This command works exactly like {@link #sdiff(String...) SDIFF} but
     * instead of being returned the resulting set is stored in dstkey.
     * 
     * @param dstkey
     * @param keys
     * @return Status code reply
     */
    public Long sdiffstore(final String dstkey, final String... keys) {
        checkIsInMulti();
        client.sdiffstore(dstkey, keys);
        return client.getIntegerReply();
    }

    /**
     * Return a random element from a Set, without removing the element. If the
     * Set is empty or the key does not exist, a nil object is returned.
     * <p>
     * The SPOP command does a similar work but the returned element is popped
     * (removed) from the Set.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return Bulk reply
     */
    public String srandmember(final String key) {
        checkIsInMulti();
        client.srandmember(key);
        return client.getBulkReply();
    }

    /**
     * Add the specified member having the specifeid score to the sorted set
     * stored at key. If member is already a member of the sorted set the score
     * is updated, and the element reinserted in the right position to ensure
     * sorting. If key does not exist a new sorted set with the specified member
     * as sole member is crated. If the key exists but does not hold a sorted
     * set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return Integer reply, specifically: 1 if the new element was added 0 if
     *         the element was already a member of the sorted set and the score
     *         was updated
     */
    public Long zadd(final String key, final double score, final String member) {
        checkIsInMulti();
        client.zadd(key, score, member);
        return client.getIntegerReply();
    }

    public Set<String> zrange(final String key, final int start, final int end) {
        checkIsInMulti();
        client.zrange(key, start, end);
        final List<String> members = client.getMultiBulkReply();
        return new LinkedHashSet<String>(members);
    }

    /**
     * Remove the specified member from the sorted set value stored at key. If
     * member was not a member of the set no operation is performed. If key does
     * not not hold a set value an error is returned.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * 
     * 
     * @param key
     * @param member
     * @return Integer reply, specifically: 1 if the new element was removed 0
     *         if the new element was not a member of the set
     */
    public Long zrem(final String key, final String member) {
        checkIsInMulti();
        client.zrem(key, member);
        return client.getIntegerReply();
    }

    /**
     * If member already exists in the sorted set adds the increment to its
     * score and updates the position of the element in the sorted set
     * accordingly. If member does not already exist in the sorted set it is
     * added with increment as score (that is, like if the previous score was
     * virtually zero). If key does not exist a new sorted set with the
     * specified member as sole member is crated. If the key exists but does not
     * hold a sorted set value an error is returned.
     * <p>
     * The score value can be the string representation of a double precision
     * floating point number. It's possible to provide a negative value to
     * perform a decrement.
     * <p>
     * For an introduction to sorted sets check the Introduction to Redis data
     * types page.
     * <p>
     * Time complexity O(log(N)) with N being the number of elements in the
     * sorted set
     * 
     * @param key
     * @param score
     * @param member
     * @return The new score
     */
    public Double zincrby(final String key, final double score,
            final String member) {
        checkIsInMulti();
        client.zincrby(key, score, member);
        String newscore = client.getBulkReply();
        return Double.valueOf(newscore);
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from low to high.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrevrank(String, String)
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrank(final String key, final String member) {
        checkIsInMulti();
        client.zrank(key, member);
        return client.getIntegerReply();
    }

    /**
     * Return the rank (or index) or member in the sorted set at key, with
     * scores being ordered from high to low.
     * <p>
     * When the given member does not exist in the sorted set, the special value
     * 'nil' is returned. The returned rank (or index) of the member is 0-based
     * for both commands.
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))
     * 
     * @see #zrank(String, String)
     * 
     * @param key
     * @param member
     * @return Integer reply or a nil bulk reply, specifically: the rank of the
     *         element as an integer reply if the element exists. A nil bulk
     *         reply if there is no such element.
     */
    public Long zrevrank(final String key, final String member) {
        checkIsInMulti();
        client.zrevrank(key, member);
        return client.getIntegerReply();
    }

    public Set<String> zrevrange(final String key, final int start,
            final int end) {
        checkIsInMulti();
        client.zrevrange(key, start, end);
        final List<String> members = client.getMultiBulkReply();
        return new LinkedHashSet<String>(members);
    }

    public Set<Tuple> zrangeWithScores(final String key, final int start,
            final int end) {
        checkIsInMulti();
        client.zrangeWithScores(key, start, end);
        Set<Tuple> set = getTupledSet();
        return set;
    }

    public Set<Tuple> zrevrangeWithScores(final String key, final int start,
            final int end) {
        checkIsInMulti();
        client.zrevrangeWithScores(key, start, end);
        Set<Tuple> set = getTupledSet();
        return set;
    }

    /**
     * Return the sorted set cardinality (number of elements). If the key does
     * not exist 0 is returned, like for empty sorted sets.
     * <p>
     * Time complexity O(1)
     * 
     * @param key
     * @return the cardinality (number of elements) of the set as an integer.
     */
    public Long zcard(final String key) {
        checkIsInMulti();
        client.zcard(key);
        return client.getIntegerReply();
    }

    /**
     * Return the score of the specified element of the sorted set at key. If
     * the specified element does not exist in the sorted set, or the key does
     * not exist at all, a special 'nil' value is returned.
     * <p>
     * <b>Time complexity:</b> O(1)
     * 
     * @param key
     * @param member
     * @return the score
     */
    public Double zscore(final String key, final String member) {
        checkIsInMulti();
        client.zscore(key, member);
        final String score = client.getBulkReply();
        return (score != null ? new Double(score) : null);
    }

    public String watch(final String... keys) {
        client.watch(keys);
        return client.getStatusCodeReply();
    }

    /**
     * Sort a Set or a List.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key.
     * By default sorting is numeric with elements being compared as double
     * precision floating point numbers. This is the simplest form of SORT.
     * 
     * @see #sort(String, String)
     * @see #sort(String, SortingParams)
     * @see #sort(String, SortingParams, String)
     * 
     * 
     * @param key
     * @return Assuming the Set/List at key contains a list of numbers, the
     *         return value will be the list of numbers ordered from the
     *         smallest to the biggest number.
     */
    public List<String> sort(final String key) {
        checkIsInMulti();
        client.sort(key);
        return client.getMultiBulkReply();
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters.
     * <p>
     * <b>examples:</b>
     * <p>
     * Given are the following sets and key/values:
     * 
     * <pre>
     * x = [1, 2, 3]
     * y = [a, b, c]
     * 
     * k1 = z
     * k2 = y
     * k3 = x
     * 
     * w1 = 9
     * w2 = 8
     * w3 = 7
     * </pre>
     * 
     * Sort Order:
     * 
     * <pre>
     * sort(x) or sort(x, sp.asc())
     * -> [1, 2, 3]
     * 
     * sort(x, sp.desc())
     * -> [3, 2, 1]
     * 
     * sort(y)
     * -> [c, a, b]
     * 
     * sort(y, sp.alpha())
     * -> [a, b, c]
     * 
     * sort(y, sp.alpha().desc())
     * -> [c, a, b]
     * </pre>
     * 
     * Limit (e.g. for Pagination):
     * 
     * <pre>
     * sort(x, sp.limit(0, 2))
     * -> [1, 2]
     * 
     * sort(y, sp.alpha().desc().limit(1, 2))
     * -> [b, a]
     * </pre>
     * 
     * Sorting by external keys:
     * 
     * <pre>
     * sort(x, sb.by(w*))
     * -> [3, 2, 1]
     * 
     * sort(x, sb.by(w*).desc())
     * -> [1, 2, 3]
     * </pre>
     * 
     * Getting external keys:
     * 
     * <pre>
     * sort(x, sp.by(w*).get(k*))
     * -> [x, y, z]
     * 
     * sort(x, sp.by(w*).get(#).get(k*))
     * -> [3, x, 2, y, 1, z]
     * </pre>
     * 
     * @see #sort(String)
     * @see #sort(String, SortingParams, String)
     * 
     * @param key
     * @param sortingParameters
     * @return a list of sorted elements.
     */
    public List<String> sort(final String key,
            final SortingParams sortingParameters) {
        checkIsInMulti();
        client.sort(key, sortingParameters);
        return client.getMultiBulkReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #brpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<String> blpop(final int timeout, final String... keys) {
        checkIsInMulti();
        List<String> args = new ArrayList<String>();
        for (String arg : keys) {
            args.add(arg);
        }
        args.add(String.valueOf(timeout));

        client.blpop(args.toArray(new String[args.size()]));
        client.setTimeoutInfinite();
        final List<String> multiBulkReply = client.getMultiBulkReply();
        client.rollbackTimeout();
        return multiBulkReply;
    }

    /**
     * Sort a Set or a List accordingly to the specified parameters and store
     * the result at dstkey.
     * 
     * @see #sort(String, SortingParams)
     * @see #sort(String)
     * @see #sort(String, String)
     * 
     * @param key
     * @param sortingParameters
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final String key, final SortingParams sortingParameters,
            final String dstkey) {
        checkIsInMulti();
        client.sort(key, sortingParameters, dstkey);
        return client.getIntegerReply();
    }

    /**
     * Sort a Set or a List and Store the Result at dstkey.
     * <p>
     * Sort the elements contained in the List, Set, or Sorted Set value at key
     * and store the result at dstkey. By default sorting is numeric with
     * elements being compared as double precision floating point numbers. This
     * is the simplest form of SORT.
     * 
     * @see #sort(String)
     * @see #sort(String, SortingParams)
     * @see #sort(String, SortingParams, String)
     * 
     * @param key
     * @param dstkey
     * @return The number of elements of the list at dstkey.
     */
    public Long sort(final String key, final String dstkey) {
        checkIsInMulti();
        client.sort(key, dstkey);
        return client.getIntegerReply();
    }

    /**
     * BLPOP (and BRPOP) is a blocking list pop primitive. You can see this
     * commands as blocking versions of LPOP and RPOP able to block if the
     * specified keys don't exist or contain empty lists.
     * <p>
     * The following is a description of the exact semantic. We describe BLPOP
     * but the two commands are identical, the only difference is that BLPOP
     * pops the element from the left (head) of the list, and BRPOP pops from
     * the right (tail).
     * <p>
     * <b>Non blocking behavior</b>
     * <p>
     * When BLPOP is called, if at least one of the specified keys contain a non
     * empty list, an element is popped from the head of the list and returned
     * to the caller together with the name of the key (BLPOP returns a two
     * elements array, the first element is the key, the second the popped
     * value).
     * <p>
     * Keys are scanned from left to right, so for instance if you issue BLPOP
     * list1 list2 list3 0 against a dataset where list1 does not exist but
     * list2 and list3 contain non empty lists, BLPOP guarantees to return an
     * element from the list stored at list2 (since it is the first non empty
     * list starting from the left).
     * <p>
     * <b>Blocking behavior</b>
     * <p>
     * If none of the specified keys exist or contain non empty lists, BLPOP
     * blocks until some other client performs a LPUSH or an RPUSH operation
     * against one of the lists.
     * <p>
     * Once new data is present on one of the lists, the client finally returns
     * with the name of the key unblocking it and the popped value.
     * <p>
     * When blocking, if a non-zero timeout is specified, the client will
     * unblock returning a nil special value if the specified amount of seconds
     * passed without a push operation against at least one of the specified
     * keys.
     * <p>
     * The timeout argument is interpreted as an integer value. A timeout of
     * zero means instead to block forever.
     * <p>
     * <b>Multiple clients blocking for the same keys</b>
     * <p>
     * Multiple clients can block for the same key. They are put into a queue,
     * so the first to be served will be the one that started to wait earlier,
     * in a first-blpopping first-served fashion.
     * <p>
     * <b>blocking POP inside a MULTI/EXEC transaction</b>
     * <p>
     * BLPOP and BRPOP can be used with pipelining (sending multiple commands
     * and reading the replies in batch), but it does not make sense to use
     * BLPOP or BRPOP inside a MULTI/EXEC block (a Redis transaction).
     * <p>
     * The behavior of BLPOP inside MULTI/EXEC when the list is empty is to
     * return a multi-bulk nil reply, exactly what happens when the timeout is
     * reached. If you like science fiction, think at it like if inside
     * MULTI/EXEC the time will flow at infinite speed :)
     * <p>
     * Time complexity: O(1)
     * 
     * @see #blpop(int, String...)
     * 
     * @param timeout
     * @param keys
     * @return BLPOP returns a two-elements array via a multi bulk reply in
     *         order to return both the unblocking key and the popped value.
     *         <p>
     *         When a non-zero timeout is specified, and the BLPOP operation
     *         timed out, the return value is a nil multi bulk reply. Most
     *         client values will return false or nil accordingly to the
     *         programming language used.
     */
    public List<String> brpop(final int timeout, final String... keys) {
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

    /**
     * Request for authentication in a password protected Redis server. A Redis
     * server can be instructed to require a password before to allow clients to
     * issue commands. This is done using the requirepass directive in the Redis
     * configuration file. If the password given by the client is correct the
     * server replies with an OK status code reply and starts accepting commands
     * from the client. Otherwise an error is returned and the clients needs to
     * try a new password. Note that for the high performance nature of Redis it
     * is possible to try a lot of passwords in parallel in very short time, so
     * make sure to generate a strong and very long password so that this attack
     * is infeasible.
     * 
     * @param password
     * @return Status code reply
     */

    @Override
    public String auth(final String password) {
        checkIsInMulti();
        client.auth(password);
        return client.getStatusCodeReply();
    }

    @Override
    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        checkIsInMulti();
        connect();
        client.setTimeoutInfinite();
        jedisPubSub.proceed(client, channels);
        client.rollbackTimeout();
    }

    @Override
    public Long publish(String channel, String message) {
        checkIsInMulti();
        client.publish(channel, message);
        return client.getIntegerReply();
    }

    @Override
    public void psubscribe(JedisPubSub jedisPubSub, String... patterns) {
        checkIsInMulti();
        connect();
        client.setTimeoutInfinite();
        jedisPubSub.proceedWithPatterns(client, patterns);
        client.rollbackTimeout();
    }

    public Long zcount(final String key, final double min, final double max) {
        checkIsInMulti();
        client.zcount(key, min, max);
        return client.getIntegerReply();
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, String, String)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<String> zrangeByScore(final String key, final double min,
            final double max) {
        checkIsInMulti();
        client.zrangeByScore(key, min, max);
        return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrangeByScore(final String key, final String min,
            final String max) {
        checkIsInMulti();
        client.zrangeByScore(key, min, max);
        return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<String> zrangeByScore(final String key, final double min,
            final double max, final int offset, final int count) {
        checkIsInMulti();
        client.zrangeByScore(key, min, max, offset, count);
        return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final String key,
            final double min, final double max) {
        checkIsInMulti();
        client.zrangeByScoreWithScores(key, min, max);
        Set<Tuple> set = getTupledSet();
        return set;
    }

    /**
     * Return the all the elements in the sorted set at key with a score between
     * min and max (including elements with score equal to min or max).
     * <p>
     * The elements having the same score are returned sorted lexicographically
     * as ASCII strings (this follows from a property of Redis sorted sets and
     * does not involve further computation).
     * <p>
     * Using the optional
     * {@link #zrangeByScore(String, double, double, int, int) LIMIT} it's
     * possible to get only a range of the matching elements in an SQL-alike
     * way. Note that if offset is large the commands needs to traverse the list
     * for offset elements and this adds up to the O(M) figure.
     * <p>
     * The {@link #zcount(String, double, double) ZCOUNT} command is similar to
     * {@link #zrangeByScore(String, double, double) ZRANGEBYSCORE} but instead
     * of returning the actual elements in the specified interval, it just
     * returns the number of matching elements.
     * <p>
     * <b>Exclusive intervals and infinity</b>
     * <p>
     * min and max can be -inf and +inf, so that you are not required to know
     * what's the greatest or smallest element in order to take, for instance,
     * elements "up to a given value".
     * <p>
     * Also while the interval is for default closed (inclusive) it's possible
     * to specify open intervals prefixing the score with a "(" character, so
     * for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (1.3 5}
     * <p>
     * Will return all the values with score > 1.3 and <= 5, while for instance:
     * <p>
     * {@code ZRANGEBYSCORE zset (5 (10}
     * <p>
     * Will return all the values with score > 5 and < 10 (5 and 10 excluded).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements returned by the command, so if M is constant
     * (for instance you always ask for the first ten elements with LIMIT) you
     * can consider it O(log(N))
     * 
     * @see #zrangeByScore(String, double, double)
     * @see #zrangeByScore(String, double, double, int, int)
     * @see #zrangeByScoreWithScores(String, double, double)
     * @see #zrangeByScoreWithScores(String, double, double, int, int)
     * @see #zcount(String, double, double)
     * 
     * @param key
     * @param min
     * @param max
     * @return Multi bulk reply specifically a list of elements in the specified
     *         score range.
     */
    public Set<Tuple> zrangeByScoreWithScores(final String key,
            final double min, final double max, final int offset,
            final int count) {
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

    public Set<String> zrevrangeByScore(final String key, final double max,
            final double min) {
        checkIsInMulti();
        client.zrevrangeByScore(key, max, min);
        return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrevrangeByScore(final String key, final String max,
            final String min) {
        checkIsInMulti();
        client.zrevrangeByScore(key, max, min);
        return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<String> zrevrangeByScore(final String key, final double max,
            final double min, final int offset, final int count) {
        checkIsInMulti();
        client.zrevrangeByScore(key, max, min, offset, count);
        return new LinkedHashSet<String>(client.getMultiBulkReply());
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
            final double max, final double min) {
        checkIsInMulti();
        client.zrevrangeByScoreWithScores(key, max, min);
        Set<Tuple> set = getTupledSet();
        return set;
    }

    public Set<Tuple> zrevrangeByScoreWithScores(final String key,
            final double max, final double min, final int offset,
            final int count) {
        checkIsInMulti();
        client.zrevrangeByScoreWithScores(key, max, min, offset, count);
        Set<Tuple> set = getTupledSet();
        return set;
    }

    /**
     * Remove all elements in the sorted set at key with rank between start and
     * end. Start and end are 0-based with rank 0 being the element with the
     * lowest score. Both start and end can be negative numbers, where they
     * indicate offsets starting at the element with the highest rank. For
     * example: -1 is the element with the highest score, -2 the element with
     * the second highest score and so forth.
     * <p>
     * <b>Time complexity:</b> O(log(N))+O(M) with N being the number of
     * elements in the sorted set and M the number of elements removed by the
     * operation
     * 
     */
    public Long zremrangeByRank(final String key, final int start, final int end) {
        checkIsInMulti();
        client.zremrangeByRank(key, start, end);
        return client.getIntegerReply();
    }

    /**
     * Remove all the elements in the sorted set at key with a score between min
     * and max (including elements with score equal to min or max).
     * <p>
     * <b>Time complexity:</b>
     * <p>
     * O(log(N))+O(M) with N being the number of elements in the sorted set and
     * M the number of elements removed by the operation
     * 
     * @param key
     * @param start
     * @param end
     * @return Integer reply, specifically the number of elements removed.
     */
    public Long zremrangeByScore(final String key, final double start,
            final double end) {
        checkIsInMulti();
        client.zremrangeByScore(key, start, end);
        return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final String dstkey, final String... sets) {
        checkIsInMulti();
        client.zunionstore(dstkey, sets);
        return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zunionstore(final String dstkey, final ZParams params,
            final String... sets) {
        checkIsInMulti();
        client.zunionstore(dstkey, params, sets);
        return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final String dstkey, final String... sets) {
        checkIsInMulti();
        client.zinterstore(dstkey, sets);
        return client.getIntegerReply();
    }

    /**
     * Creates a union or intersection of N sorted sets given by keys k1 through
     * kN, and stores it at dstkey. It is mandatory to provide the number of
     * input keys N, before passing the input keys and the other (optional)
     * arguments.
     * <p>
     * As the terms imply, the {@link #zinterstore(String, String...)
     * ZINTERSTORE} command requires an element to be present in each of the
     * given inputs to be inserted in the result. The
     * {@link #zunionstore(String, String...) ZUNIONSTORE} command inserts all
     * elements across all inputs.
     * <p>
     * Using the WEIGHTS option, it is possible to add weight to each input
     * sorted set. This means that the score of each element in the sorted set
     * is first multiplied by this weight before being passed to the
     * aggregation. When this option is not given, all weights default to 1.
     * <p>
     * With the AGGREGATE option, it's possible to specify how the results of
     * the union or intersection are aggregated. This option defaults to SUM,
     * where the score of an element is summed across the inputs where it
     * exists. When this option is set to be either MIN or MAX, the resulting
     * set will contain the minimum or maximum score of an element across the
     * inputs where it exists.
     * <p>
     * <b>Time complexity:</b> O(N) + O(M log(M)) with N being the sum of the
     * sizes of the input sorted sets, and M being the number of elements in the
     * resulting sorted set
     * 
     * @see #zunionstore(String, String...)
     * @see #zunionstore(String, ZParams, String...)
     * @see #zinterstore(String, String...)
     * @see #zinterstore(String, ZParams, String...)
     * 
     * @param dstkey
     * @param sets
     * @param params
     * @return Integer reply, specifically the number of elements in the sorted
     *         set at dstkey
     */
    public Long zinterstore(final String dstkey, final ZParams params,
            final String... sets) {
        checkIsInMulti();
        client.zinterstore(dstkey, params, sets);
        return client.getIntegerReply();
    }

    public Long strlen(final String key) {
        client.strlen(key);
        return client.getIntegerReply();
    }

    public Long lpushx(final String key, final String string) {
        client.lpushx(key, string);
        return client.getIntegerReply();
    }

    /**
     * Undo a {@link #expire(String, int) expire} at turning the expire key into
     * a normal key.
     * <p>
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, specifically: 1: the key is now persist. 0: the
     *         key is not persist (only happens when key not set).
     */
    public Long persist(final String key) {
        client.persist(key);
        return client.getIntegerReply();
    }

    public Long rpushx(final String key, final String string) {
        client.rpushx(key, string);
        return client.getIntegerReply();
    }

    public String echo(final String string) {
        client.echo(string);
        return client.getBulkReply();
    }

    public Long linsert(final String key, final LIST_POSITION where,
            final String pivot, final String value) {
        client.linsert(key, where, pivot, value);
        return client.getIntegerReply();
    }

    /**
     * Pop a value from a list, push it to another list and return it; or block
     * until one is available
     * 
     * @param source
     * @param destination
     * @param timeout
     * @return the element
     */
    public String brpoplpush(String source, String destination, int timeout) {
        client.brpoplpush(source, destination, timeout);
        client.setTimeoutInfinite();
        String reply = client.getBulkReply();
        client.rollbackTimeout();
        return reply;
    }

    /**
     * Sets or clears the bit at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @param value
     * @return
     */
    public boolean setbit(String key, long offset, boolean value) {
        client.setbit(key, offset, value);
        return client.getIntegerReply() == 1;
    }

    /**
     * Returns the bit value at offset in the string value stored at key
     * 
     * @param key
     * @param offset
     * @return
     */
    public boolean getbit(String key, long offset) {
        client.getbit(key, offset);
        return client.getIntegerReply() == 1;
    }

    public long setrange(String key, long offset, String value) {
        client.setrange(key, offset, value);
        return client.getIntegerReply();
    }

    public String getrange(String key, long startOffset, long endOffset) {
        client.getrange(key, startOffset, endOffset);
        return client.getBulkReply();
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

package org.elasticsearch.common.math;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

/**
 * Some useful additions to the built-in functions in {@link Math}.
 *
 * @version $Revision: 927249 $ $Date: 2010-03-24 21:06:51 -0400 (Wed, 24 Mar 2010) $
 */
public final class MathUtils {


    /**
     * Smallest positive number such that 1 - EPSILON is not numerically equal to 1.
     */
    public static final double EPSILON = 0x1.0p-53;

    /**
     * Safe minimum, such that 1 / SAFE_MIN does not overflow.
     * <p>In IEEE 754 arithmetic, this is also the smallest normalized
     * number 2<sup>-1022</sup>.</p>
     */
    public static final double SAFE_MIN = 0x1.0p-1022;

    /**
     * 2 &pi;.
     *
     * @since 2.1
     */
    public static final double TWO_PI = 2 * Math.PI;

    /**
     * -1.0 cast as a byte.
     */
    private static final byte NB = (byte) -1;

    /**
     * -1.0 cast as a short.
     */
    private static final short NS = (short) -1;

    /**
     * 1.0 cast as a byte.
     */
    private static final byte PB = (byte) 1;

    /**
     * 1.0 cast as a short.
     */
    private static final short PS = (short) 1;

    /**
     * 0.0 cast as a byte.
     */
    private static final byte ZB = (byte) 0;

    /**
     * 0.0 cast as a short.
     */
    private static final short ZS = (short) 0;

    /**
     * Gap between NaN and regular numbers.
     */
    private static final int NAN_GAP = 4 * 1024 * 1024;

    /**
     * Offset to order signed double numbers lexicographically.
     */
    private static final long SGN_MASK = 0x8000000000000000L;

    /**
     * All long-representable factorials
     */
    private static final long[] FACTORIALS = new long[]{
            1l, 1l, 2l,
            6l, 24l, 120l,
            720l, 5040l, 40320l,
            362880l, 3628800l, 39916800l,
            479001600l, 6227020800l, 87178291200l,
            1307674368000l, 20922789888000l, 355687428096000l,
            6402373705728000l, 121645100408832000l, 2432902008176640000l};

    /**
     * Private Constructor
     */
    private MathUtils() {
        super();
    }

    /**
     * Add two integers, checking for overflow.
     *
     * @param x an addend
     * @param y an addend
     * @return the sum <code>x+y</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             int
     * @since 1.1
     */
    public static int addAndCheck(int x, int y) {
        long s = (long) x + (long) y;
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: add");
        }
        return (int) s;
    }

    /**
     * Add two long integers, checking for overflow.
     *
     * @param a an addend
     * @param b an addend
     * @return the sum <code>a+b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    public static long addAndCheck(long a, long b) {
        return addAndCheck(a, b, "overflow: add");
    }

    /**
     * Add two long integers, checking for overflow.
     *
     * @param a   an addend
     * @param b   an addend
     * @param msg the message to use for any thrown exception.
     * @return the sum <code>a+b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    private static long addAndCheck(long a, long b, String msg) {
        long ret;
        if (a > b) {
            // use symmetry to reduce boundary cases
            ret = addAndCheck(b, a, msg);
        } else {
            // assert a <= b

            if (a < 0) {
                if (b < 0) {
                    // check for negative overflow
                    if (Long.MIN_VALUE - b <= a) {
                        ret = a + b;
                    } else {
                        throw new ArithmeticException(msg);
                    }
                } else {
                    // opposite sign addition is always safe
                    ret = a + b;
                }
            } else {
                // assert a >= 0
                // assert b >= 0

                // check for positive overflow
                if (a <= Long.MAX_VALUE - b) {
                    ret = a + b;
                } else {
                    throw new ArithmeticException(msg);
                }
            }
        }
        return ret;
    }

    /**
     * Returns an exact representation of the <a
     * href="http://mathworld.wolfram.com/BinomialCoefficient.html"> Binomial
     * Coefficient</a>, "<code>n choose k</code>", the number of
     * <code>k</code>-element subsets that can be selected from an
     * <code>n</code>-element set.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>0 <= k <= n </code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>long</code>. The
     * largest value of <code>n</code> for which all coefficients are
     * <code> < Long.MAX_VALUE</code> is 66. If the computed value exceeds
     * <code>Long.MAX_VALUE</code> an <code>ArithMeticException</code> is
     * thrown.</li>
     * </ul></p>
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @return <code>n choose k</code>
     * @throws IllegalArgumentException if preconditions are not met.
     * @throws ArithmeticException      if the result is too large to be represented
     *                                  by a long integer.
     */
    public static long binomialCoefficient(final int n, final int k) {
        checkBinomial(n, k);
        if ((n == k) || (k == 0)) {
            return 1;
        }
        if ((k == 1) || (k == n - 1)) {
            return n;
        }
        // Use symmetry for large k
        if (k > n / 2)
            return binomialCoefficient(n, n - k);

        // We use the formula
        // (n choose k) = n! / (n-k)! / k!
        // (n choose k) == ((n-k+1)*...*n) / (1*...*k)
        // which could be written
        // (n choose k) == (n-1 choose k-1) * n / k
        long result = 1;
        if (n <= 61) {
            // For n <= 61, the naive implementation cannot overflow.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                result = result * i / j;
                i++;
            }
        } else if (n <= 66) {
            // For n > 61 but n <= 66, the result cannot overflow,
            // but we must take care not to overflow intermediate values.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                // We know that (result * i) is divisible by j,
                // but (result * i) may overflow, so we split j:
                // Filter out the gcd, d, so j/d and i/d are integer.
                // result is divisible by (j/d) because (j/d)
                // is relative prime to (i/d) and is a divisor of
                // result * (i/d).
                final long d = gcd(i, j);
                result = (result / (j / d)) * (i / d);
                i++;
            }
        } else {
            // For n > 66, a result overflow might occur, so we check
            // the multiplication, taking care to not overflow
            // unnecessary.
            int i = n - k + 1;
            for (int j = 1; j <= k; j++) {
                final long d = gcd(i, j);
                result = mulAndCheck(result / (j / d), i / d);
                i++;
            }
        }
        return result;
    }

    /**
     * Returns a <code>double</code> representation of the <a
     * href="http://mathworld.wolfram.com/BinomialCoefficient.html"> Binomial
     * Coefficient</a>, "<code>n choose k</code>", the number of
     * <code>k</code>-element subsets that can be selected from an
     * <code>n</code>-element set.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>0 <= k <= n </code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>double</code>. The
     * largest value of <code>n</code> for which all coefficients are <
     * Double.MAX_VALUE is 1029. If the computed value exceeds Double.MAX_VALUE,
     * Double.POSITIVE_INFINITY is returned</li>
     * </ul></p>
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @return <code>n choose k</code>
     * @throws IllegalArgumentException if preconditions are not met.
     */
    public static double binomialCoefficientDouble(final int n, final int k) {
        checkBinomial(n, k);
        if ((n == k) || (k == 0)) {
            return 1d;
        }
        if ((k == 1) || (k == n - 1)) {
            return n;
        }
        if (k > n / 2) {
            return binomialCoefficientDouble(n, n - k);
        }
        if (n < 67) {
            return binomialCoefficient(n, k);
        }

        double result = 1d;
        for (int i = 1; i <= k; i++) {
            result *= (double) (n - k + i) / (double) i;
        }

        return Math.floor(result + 0.5);
    }

    /**
     * Returns the natural <code>log</code> of the <a
     * href="http://mathworld.wolfram.com/BinomialCoefficient.html"> Binomial
     * Coefficient</a>, "<code>n choose k</code>", the number of
     * <code>k</code>-element subsets that can be selected from an
     * <code>n</code>-element set.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>0 <= k <= n </code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * </ul></p>
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @return <code>n choose k</code>
     * @throws IllegalArgumentException if preconditions are not met.
     */
    public static double binomialCoefficientLog(final int n, final int k) {
        checkBinomial(n, k);
        if ((n == k) || (k == 0)) {
            return 0;
        }
        if ((k == 1) || (k == n - 1)) {
            return Math.log(n);
        }

        /*
         * For values small enough to do exact integer computation,
         * return the log of the exact value
         */
        if (n < 67) {
            return Math.log(binomialCoefficient(n, k));
        }

        /*
         * Return the log of binomialCoefficientDouble for values that will not
         * overflow binomialCoefficientDouble
         */
        if (n < 1030) {
            return Math.log(binomialCoefficientDouble(n, k));
        }

        if (k > n / 2) {
            return binomialCoefficientLog(n, n - k);
        }

        /*
         * Sum logs for values that could overflow
         */
        double logSum = 0;

        // n!/(n-k)!
        for (int i = n - k + 1; i <= n; i++) {
            logSum += Math.log(i);
        }

        // divide by k!
        for (int i = 2; i <= k; i++) {
            logSum -= Math.log(i);
        }

        return logSum;
    }

    /**
     * Check binomial preconditions.
     *
     * @param n the size of the set
     * @param k the size of the subsets to be counted
     * @throws IllegalArgumentException if preconditions are not met.
     */
    private static void checkBinomial(final int n, final int k)
            throws IllegalArgumentException {
        if (n < k) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= k for binomial coefficient (n,k), got n = {0}, k = {1}",
                    n, k);
        }
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for binomial coefficient (n,k), got n = {0}",
                    n);
        }
    }

    /**
     * Compares two numbers given some amount of allowed error.
     *
     * @param x   the first number
     * @param y   the second number
     * @param eps the amount of error to allow when checking for equality
     * @return <ul><li>0 if  {@link #equals(double, double, double) equals(x, y, eps)}</li>
     *         <li>&lt; 0 if !{@link #equals(double, double, double) equals(x, y, eps)} &amp;&amp; x &lt; y</li>
     *         <li>> 0 if !{@link #equals(double, double, double) equals(x, y, eps)} &amp;&amp; x > y</li></ul>
     */
    public static int compareTo(double x, double y, double eps) {
        if (equals(x, y, eps)) {
            return 0;
        } else if (x < y) {
            return -1;
        }
        return 1;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/HyperbolicCosine.html">
     * hyperbolic cosine</a> of x.
     *
     * @param x double value for which to find the hyperbolic cosine
     * @return hyperbolic cosine of x
     */
    public static double cosh(double x) {
        return (Math.exp(x) + Math.exp(-x)) / 2.0;
    }

    /**
     * Returns true iff both arguments are NaN or neither is NaN and they are
     * equal
     *
     * @param x first value
     * @param y second value
     * @return true if the values are equal or both are NaN
     */
    public static boolean equals(double x, double y) {
        return (Double.isNaN(x) && Double.isNaN(y)) || x == y;
    }

    /**
     * Returns true iff both arguments are equal or within the range of allowed
     * error (inclusive).
     * <p>
     * Two NaNs are considered equals, as are two infinities with same sign.
     * </p>
     *
     * @param x   first value
     * @param y   second value
     * @param eps the amount of absolute error to allow
     * @return true if the values are equal or within range of each other
     */
    public static boolean equals(double x, double y, double eps) {
        return equals(x, y) || (Math.abs(y - x) <= eps);
    }

    /**
     * Returns true iff both arguments are equal or within the range of allowed
     * error (inclusive).
     * Adapted from <a
     * href="http://www.cygnus-software.com/papers/comparingfloats/comparingfloats.htm">
     * Bruce Dawson</a>
     *
     * @param x       first value
     * @param y       second value
     * @param maxUlps {@code (maxUlps - 1)} is the number of floating point
     *                values between {@code x} and {@code y}.
     * @return {@code true} if there are less than {@code maxUlps} floating
     *         point values between {@code x} and {@code y}
     */
    public static boolean equals(double x, double y, int maxUlps) {
        // Check that "maxUlps" is non-negative and small enough so that the
        // default NAN won't compare as equal to anything.
        assert maxUlps > 0 && maxUlps < NAN_GAP;

        long xInt = Double.doubleToLongBits(x);
        long yInt = Double.doubleToLongBits(y);

        // Make lexicographically ordered as a two's-complement integer.
        if (xInt < 0) {
            xInt = SGN_MASK - xInt;
        }
        if (yInt < 0) {
            yInt = SGN_MASK - yInt;
        }

        return Math.abs(xInt - yInt) <= maxUlps;
    }

    /**
     * Returns true iff both arguments are null or have same dimensions
     * and all their elements are {@link #equals(double,double) equals}
     *
     * @param x first array
     * @param y second array
     * @return true if the values are both null or have same dimension
     *         and equal elements
     * @since 1.2
     */
    public static boolean equals(double[] x, double[] y) {
        if ((x == null) || (y == null)) {
            return !((x == null) ^ (y == null));
        }
        if (x.length != y.length) {
            return false;
        }
        for (int i = 0; i < x.length; ++i) {
            if (!equals(x[i], y[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns n!. Shorthand for <code>n</code> <a
     * href="http://mathworld.wolfram.com/Factorial.html"> Factorial</a>, the
     * product of the numbers <code>1,...,n</code>.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>n >= 0</code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>long</code>. The
     * largest value of <code>n</code> for which <code>n!</code> <
     * Long.MAX_VALUE</code> is 20. If the computed value exceeds <code>Long.MAX_VALUE</code>
     * an <code>ArithMeticException </code> is thrown.</li>
     * </ul>
     * </p>
     *
     * @param n argument
     * @return <code>n!</code>
     * @throws ArithmeticException      if the result is too large to be represented
     *                                  by a long integer.
     * @throws IllegalArgumentException if n < 0
     */
    public static long factorial(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for n!, got n = {0}",
                    n);
        }
        if (n > 20) {
            throw new ArithmeticException(
                    "factorial value is too large to fit in a long");
        }
        return FACTORIALS[n];
    }

    /**
     * Returns n!. Shorthand for <code>n</code> <a
     * href="http://mathworld.wolfram.com/Factorial.html"> Factorial</a>, the
     * product of the numbers <code>1,...,n</code> as a <code>double</code>.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>n >= 0</code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * <li> The result is small enough to fit into a <code>double</code>. The
     * largest value of <code>n</code> for which <code>n!</code> <
     * Double.MAX_VALUE</code> is 170. If the computed value exceeds
     * Double.MAX_VALUE, Double.POSITIVE_INFINITY is returned</li>
     * </ul>
     * </p>
     *
     * @param n argument
     * @return <code>n!</code>
     * @throws IllegalArgumentException if n < 0
     */
    public static double factorialDouble(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for n!, got n = {0}",
                    n);
        }
        if (n < 21) {
            return factorial(n);
        }
        return Math.floor(Math.exp(factorialLog(n)) + 0.5);
    }

    /**
     * Returns the natural logarithm of n!.
     * <p>
     * <Strong>Preconditions</strong>:
     * <ul>
     * <li> <code>n >= 0</code> (otherwise
     * <code>IllegalArgumentException</code> is thrown)</li>
     * </ul></p>
     *
     * @param n argument
     * @return <code>n!</code>
     * @throws IllegalArgumentException if preconditions are not met.
     */
    public static double factorialLog(final int n) {
        if (n < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "must have n >= 0 for n!, got n = {0}",
                    n);
        }
        if (n < 21) {
            return Math.log(factorial(n));
        }
        double logSum = 0;
        for (int i = 2; i <= n; i++) {
            logSum += Math.log(i);
        }
        return logSum;
    }

    /**
     * <p>
     * Gets the greatest common divisor of the absolute value of two numbers,
     * using the "binary gcd" method which avoids division and modulo
     * operations. See Knuth 4.5.2 algorithm B. This algorithm is due to Josef
     * Stein (1961).
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations
     * <code>gcd(Integer.MIN_VALUE, Integer.MIN_VALUE)</code>,
     * <code>gcd(Integer.MIN_VALUE, 0)</code> and
     * <code>gcd(0, Integer.MIN_VALUE)</code> throw an
     * <code>ArithmeticException</code>, because the result would be 2^31, which
     * is too large for an int value.</li>
     * <li>The result of <code>gcd(x, x)</code>, <code>gcd(0, x)</code> and
     * <code>gcd(x, 0)</code> is the absolute value of <code>x</code>, except
     * for the special cases above.
     * <li>The invocation <code>gcd(0, 0)</code> is the only one which returns
     * <code>0</code>.</li>
     * </ul>
     *
     * @param p any number
     * @param q any number
     * @return the greatest common divisor, never negative
     * @throws ArithmeticException if the result cannot be represented as a
     *                             nonnegative int value
     * @since 1.1
     */
    public static int gcd(final int p, final int q) {
        int u = p;
        int v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Integer.MIN_VALUE) || (v == Integer.MIN_VALUE)) {
                throw MathRuntimeException.createArithmeticException(
                        "overflow: gcd({0}, {1}) is 2^31",
                        p, q);
            }
            return Math.abs(u) + Math.abs(v);
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) { // while u and v are
            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 31) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: gcd({0}, {1}) is 2^31",
                    p, q);
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        int t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1 << k); // gcd is u*2^k
    }

    /**
     * <p>
     * Gets the greatest common divisor of the absolute value of two numbers,
     * using the "binary gcd" method which avoids division and modulo
     * operations. See Knuth 4.5.2 algorithm B. This algorithm is due to Josef
     * Stein (1961).
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations
     * <code>gcd(Long.MIN_VALUE, Long.MIN_VALUE)</code>,
     * <code>gcd(Long.MIN_VALUE, 0L)</code> and
     * <code>gcd(0L, Long.MIN_VALUE)</code> throw an
     * <code>ArithmeticException</code>, because the result would be 2^63, which
     * is too large for a long value.</li>
     * <li>The result of <code>gcd(x, x)</code>, <code>gcd(0L, x)</code> and
     * <code>gcd(x, 0L)</code> is the absolute value of <code>x</code>, except
     * for the special cases above.
     * <li>The invocation <code>gcd(0L, 0L)</code> is the only one which returns
     * <code>0L</code>.</li>
     * </ul>
     *
     * @param p any number
     * @param q any number
     * @return the greatest common divisor, never negative
     * @throws ArithmeticException if the result cannot be represented as a nonnegative long
     *                             value
     * @since 2.1
     */
    public static long gcd(final long p, final long q) {
        long u = p;
        long v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Long.MIN_VALUE) || (v == Long.MIN_VALUE)) {
                throw MathRuntimeException.createArithmeticException(
                        "overflow: gcd({0}, {1}) is 2^63",
                        p, q);
            }
            return Math.abs(u) + Math.abs(v);
        }
        // keep u and v negative, as negative integers range down to
        // -2^63, while positive numbers can only be as large as 2^63-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 63) { // while u and v are
            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 63) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: gcd({0}, {1}) is 2^63",
                    p, q);
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        long t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1L << k); // gcd is u*2^k
    }

    /**
     * Returns an integer hash code representing the given double value.
     *
     * @param value the value to be hashed
     * @return the hash code
     */
    public static int hash(double value) {
        return new Double(value).hashCode();
    }

    /**
     * Returns an integer hash code representing the given double array.
     *
     * @param value the value to be hashed (may be null)
     * @return the hash code
     * @since 1.2
     */
    public static int hash(double[] value) {
        return Arrays.hashCode(value);
    }

    /**
     * For a byte value x, this method returns (byte)(+1) if x >= 0 and
     * (byte)(-1) if x < 0.
     *
     * @param x the value, a byte
     * @return (byte)(+1) or (byte)(-1), depending on the sign of x
     */
    public static byte indicator(final byte x) {
        return (x >= ZB) ? PB : NB;
    }

    /**
     * For a double precision value x, this method returns +1.0 if x >= 0 and
     * -1.0 if x < 0. Returns <code>NaN</code> if <code>x</code> is
     * <code>NaN</code>.
     *
     * @param x the value, a double
     * @return +1.0 or -1.0, depending on the sign of x
     */
    public static double indicator(final double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        return (x >= 0.0) ? 1.0 : -1.0;
    }

    /**
     * For a float value x, this method returns +1.0F if x >= 0 and -1.0F if x <
     * 0. Returns <code>NaN</code> if <code>x</code> is <code>NaN</code>.
     *
     * @param x the value, a float
     * @return +1.0F or -1.0F, depending on the sign of x
     */
    public static float indicator(final float x) {
        if (Float.isNaN(x)) {
            return Float.NaN;
        }
        return (x >= 0.0F) ? 1.0F : -1.0F;
    }

    /**
     * For an int value x, this method returns +1 if x >= 0 and -1 if x < 0.
     *
     * @param x the value, an int
     * @return +1 or -1, depending on the sign of x
     */
    public static int indicator(final int x) {
        return (x >= 0) ? 1 : -1;
    }

    /**
     * For a long value x, this method returns +1L if x >= 0 and -1L if x < 0.
     *
     * @param x the value, a long
     * @return +1L or -1L, depending on the sign of x
     */
    public static long indicator(final long x) {
        return (x >= 0L) ? 1L : -1L;
    }

    /**
     * For a short value x, this method returns (short)(+1) if x >= 0 and
     * (short)(-1) if x < 0.
     *
     * @param x the value, a short
     * @return (short)(+1) or (short)(-1), depending on the sign of x
     */
    public static short indicator(final short x) {
        return (x >= ZS) ? PS : NS;
    }

    /**
     * <p>
     * Returns the least common multiple of the absolute value of two numbers,
     * using the formula <code>lcm(a,b) = (a / gcd(a,b)) * b</code>.
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations <code>lcm(Integer.MIN_VALUE, n)</code> and
     * <code>lcm(n, Integer.MIN_VALUE)</code>, where <code>abs(n)</code> is a
     * power of 2, throw an <code>ArithmeticException</code>, because the result
     * would be 2^31, which is too large for an int value.</li>
     * <li>The result of <code>lcm(0, x)</code> and <code>lcm(x, 0)</code> is
     * <code>0</code> for any <code>x</code>.
     * </ul>
     *
     * @param a any number
     * @param b any number
     * @return the least common multiple, never negative
     * @throws ArithmeticException if the result cannot be represented as a nonnegative int
     *                             value
     * @since 1.1
     */
    public static int lcm(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        int lcm = Math.abs(mulAndCheck(a / gcd(a, b), b));
        if (lcm == Integer.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: lcm({0}, {1}) is 2^31",
                    a, b);
        }
        return lcm;
    }

    /**
     * <p>
     * Returns the least common multiple of the absolute value of two numbers,
     * using the formula <code>lcm(a,b) = (a / gcd(a,b)) * b</code>.
     * </p>
     * Special cases:
     * <ul>
     * <li>The invocations <code>lcm(Long.MIN_VALUE, n)</code> and
     * <code>lcm(n, Long.MIN_VALUE)</code>, where <code>abs(n)</code> is a
     * power of 2, throw an <code>ArithmeticException</code>, because the result
     * would be 2^63, which is too large for an int value.</li>
     * <li>The result of <code>lcm(0L, x)</code> and <code>lcm(x, 0L)</code> is
     * <code>0L</code> for any <code>x</code>.
     * </ul>
     *
     * @param a any number
     * @param b any number
     * @return the least common multiple, never negative
     * @throws ArithmeticException if the result cannot be represented as a nonnegative long
     *                             value
     * @since 2.1
     */
    public static long lcm(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        long lcm = Math.abs(mulAndCheck(a / gcd(a, b), b));
        if (lcm == Long.MIN_VALUE) {
            throw MathRuntimeException.createArithmeticException(
                    "overflow: lcm({0}, {1}) is 2^63",
                    a, b);
        }
        return lcm;
    }

    /**
     * <p>Returns the
     * <a href="http://mathworld.wolfram.com/Logarithm.html">logarithm</a>
     * for base <code>b</code> of <code>x</code>.
     * </p>
     * <p>Returns <code>NaN<code> if either argument is negative.  If
     * <code>base</code> is 0 and <code>x</code> is positive, 0 is returned.
     * If <code>base</code> is positive and <code>x</code> is 0,
     * <code>Double.NEGATIVE_INFINITY</code> is returned.  If both arguments
     * are 0, the result is <code>NaN</code>.</p>
     *
     * @param base the base of the logarithm, must be greater than 0
     * @param x    argument, must be greater than 0
     * @return the value of the logarithm - the number y such that base^y = x.
     * @since 1.2
     */
    public static double log(double base, double x) {
        return Math.log(x) / Math.log(base);
    }

    /**
     * Multiply two integers, checking for overflow.
     *
     * @param x a factor
     * @param y a factor
     * @return the product <code>x*y</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             int
     * @since 1.1
     */
    public static int mulAndCheck(int x, int y) {
        long m = ((long) x) * ((long) y);
        if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: mul");
        }
        return (int) m;
    }

    /**
     * Multiply two long integers, checking for overflow.
     *
     * @param a first value
     * @param b second value
     * @return the product <code>a * b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    public static long mulAndCheck(long a, long b) {
        long ret;
        String msg = "overflow: multiply";
        if (a > b) {
            // use symmetry to reduce boundary cases
            ret = mulAndCheck(b, a);
        } else {
            if (a < 0) {
                if (b < 0) {
                    // check for positive overflow with negative a, negative b
                    if (a >= Long.MAX_VALUE / b) {
                        ret = a * b;
                    } else {
                        throw new ArithmeticException(msg);
                    }
                } else if (b > 0) {
                    // check for negative overflow with negative a, positive b
                    if (Long.MIN_VALUE / b <= a) {
                        ret = a * b;
                    } else {
                        throw new ArithmeticException(msg);

                    }
                } else {
                    // assert b == 0
                    ret = 0;
                }
            } else if (a > 0) {
                // assert a > 0
                // assert b > 0

                // check for positive overflow with positive a, positive b
                if (a <= Long.MAX_VALUE / b) {
                    ret = a * b;
                } else {
                    throw new ArithmeticException(msg);
                }
            } else {
                // assert a == 0
                ret = 0;
            }
        }
        return ret;
    }

    /**
     * Get the next machine representable number after a number, moving
     * in the direction of another number.
     * <p>
     * If <code>direction</code> is greater than or equal to<code>d</code>,
     * the smallest machine representable number strictly greater than
     * <code>d</code> is returned; otherwise the largest representable number
     * strictly less than <code>d</code> is returned.</p>
     * <p>
     * If <code>d</code> is NaN or Infinite, it is returned unchanged.</p>
     *
     * @param d         base number
     * @param direction (the only important thing is whether
     *                  direction is greater or smaller than d)
     * @return the next machine representable number in the specified direction
     * @since 1.2
     */
    public static double nextAfter(double d, double direction) {

        // handling of some important special cases
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            return d;
        } else if (d == 0) {
            return (direction < 0) ? -Double.MIN_VALUE : Double.MIN_VALUE;
        }
        // special cases MAX_VALUE to infinity and  MIN_VALUE to 0
        // are handled just as normal numbers

        // split the double in raw components
        long bits = Double.doubleToLongBits(d);
        long sign = bits & 0x8000000000000000L;
        long exponent = bits & 0x7ff0000000000000L;
        long mantissa = bits & 0x000fffffffffffffL;

        if (d * (direction - d) >= 0) {
            // we should increase the mantissa
            if (mantissa == 0x000fffffffffffffL) {
                return Double.longBitsToDouble(sign |
                        (exponent + 0x0010000000000000L));
            } else {
                return Double.longBitsToDouble(sign |
                        exponent | (mantissa + 1));
            }
        } else {
            // we should decrease the mantissa
            if (mantissa == 0L) {
                return Double.longBitsToDouble(sign |
                        (exponent - 0x0010000000000000L) |
                        0x000fffffffffffffL);
            } else {
                return Double.longBitsToDouble(sign |
                        exponent | (mantissa - 1));
            }
        }

    }

    /**
     * Scale a number by 2<sup>scaleFactor</sup>.
     * <p>If <code>d</code> is 0 or NaN or Infinite, it is returned unchanged.</p>
     *
     * @param d           base number
     * @param scaleFactor power of two by which d sould be multiplied
     * @return d &times; 2<sup>scaleFactor</sup>
     * @since 2.0
     */
    public static double scalb(final double d, final int scaleFactor) {

        // handling of some important special cases
        if ((d == 0) || Double.isNaN(d) || Double.isInfinite(d)) {
            return d;
        }

        // split the double in raw components
        final long bits = Double.doubleToLongBits(d);
        final long exponent = bits & 0x7ff0000000000000L;
        final long rest = bits & 0x800fffffffffffffL;

        // shift the exponent
        final long newBits = rest | (exponent + (((long) scaleFactor) << 52));
        return Double.longBitsToDouble(newBits);

    }

    /**
     * Normalize an angle in a 2&pi wide interval around a center value.
     * <p>This method has three main uses:</p>
     * <ul>
     * <li>normalize an angle between 0 and 2&pi;:<br/>
     * <code>a = MathUtils.normalizeAngle(a, Math.PI);</code></li>
     * <li>normalize an angle between -&pi; and +&pi;<br/>
     * <code>a = MathUtils.normalizeAngle(a, 0.0);</code></li>
     * <li>compute the angle between two defining angular positions:<br>
     * <code>angle = MathUtils.normalizeAngle(end, start) - start;</code></li>
     * </ul>
     * <p>Note that due to numerical accuracy and since &pi; cannot be represented
     * exactly, the result interval is <em>closed</em>, it cannot be half-closed
     * as would be more satisfactory in a purely mathematical view.</p>
     *
     * @param a      angle to normalize
     * @param center center of the desired 2&pi; interval for the result
     * @return a-2k&pi; with integer k and center-&pi; &lt;= a-2k&pi; &lt;= center+&pi;
     * @since 1.2
     */
    public static double normalizeAngle(double a, double center) {
        return a - TWO_PI * Math.floor((a + Math.PI - center) / TWO_PI);
    }

    /**
     * <p>Normalizes an array to make it sum to a specified value.
     * Returns the result of the transformation <pre>
     *    x |-> x * normalizedSum / sum
     * </pre>
     * applied to each non-NaN element x of the input array, where sum is the
     * sum of the non-NaN entries in the input array.</p>
     *
     * <p>Throws IllegalArgumentException if <code>normalizedSum</code> is infinite
     * or NaN and ArithmeticException if the input array contains any infinite elements
     * or sums to 0</p>
     *
     * <p>Ignores (i.e., copies unchanged to the output array) NaNs in the input array.</p>
     *
     * @param values        input array to be normalized
     * @param normalizedSum target sum for the normalized array
     * @return normalized array
     * @throws ArithmeticException      if the input array contains infinite elements or sums to zero
     * @throws IllegalArgumentException if the target sum is infinite or NaN
     * @since 2.1
     */
    public static double[] normalizeArray(double[] values, double normalizedSum)
            throws ArithmeticException, IllegalArgumentException {
        if (Double.isInfinite(normalizedSum)) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "Cannot normalize to an infinite value");
        }
        if (Double.isNaN(normalizedSum)) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "Cannot normalize to NaN");
        }
        double sum = 0d;
        final int len = values.length;
        double[] out = new double[len];
        for (int i = 0; i < len; i++) {
            if (Double.isInfinite(values[i])) {
                throw MathRuntimeException.createArithmeticException(
                        "Array contains an infinite element, {0} at index {1}", values[i], i);
            }
            if (!Double.isNaN(values[i])) {
                sum += values[i];
            }
        }
        if (sum == 0) {
            throw MathRuntimeException.createArithmeticException(
                    "Array sums to zero");
        }
        for (int i = 0; i < len; i++) {
            if (Double.isNaN(values[i])) {
                out[i] = Double.NaN;
            } else {
                out[i] = values[i] * normalizedSum / sum;
            }
        }
        return out;
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @param x     the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @return the rounded value.
     * @since 1.1
     */
    public static double round(double x, int scale) {
        return round(x, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the given method which is any method defined in
     * {@link BigDecimal}.
     *
     * @param x              the value to round.
     * @param scale          the number of digits to the right of the decimal point.
     * @param roundingMethod the rounding method as defined in
     *                       {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    public static double round(double x, int scale, int roundingMethod) {
        try {
            return (new BigDecimal
                    (Double.toString(x))
                    .setScale(scale, roundingMethod))
                    .doubleValue();
        } catch (NumberFormatException ex) {
            if (Double.isInfinite(x)) {
                return x;
            } else {
                return Double.NaN;
            }
        }
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounding using the {@link BigDecimal#ROUND_HALF_UP} method.
     *
     * @param x     the value to round.
     * @param scale the number of digits to the right of the decimal point.
     * @return the rounded value.
     * @since 1.1
     */
    public static float round(float x, int scale) {
        return round(x, scale, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * Round the given value to the specified number of decimal places. The
     * value is rounded using the given method which is any method defined in
     * {@link BigDecimal}.
     *
     * @param x              the value to round.
     * @param scale          the number of digits to the right of the decimal point.
     * @param roundingMethod the rounding method as defined in
     *                       {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    public static float round(float x, int scale, int roundingMethod) {
        float sign = indicator(x);
        float factor = (float) Math.pow(10.0f, scale) * sign;
        return (float) roundUnscaled(x * factor, sign, roundingMethod) / factor;
    }

    /**
     * Round the given non-negative, value to the "nearest" integer. Nearest is
     * determined by the rounding method specified. Rounding methods are defined
     * in {@link BigDecimal}.
     *
     * @param unscaled       the value to round.
     * @param sign           the sign of the original, scaled value.
     * @param roundingMethod the rounding method as defined in
     *                       {@link BigDecimal}.
     * @return the rounded value.
     * @since 1.1
     */
    private static double roundUnscaled(double unscaled, double sign,
                                        int roundingMethod) {
        switch (roundingMethod) {
            case BigDecimal.ROUND_CEILING:
                if (sign == -1) {
                    unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                } else {
                    unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
                }
                break;
            case BigDecimal.ROUND_DOWN:
                unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                break;
            case BigDecimal.ROUND_FLOOR:
                if (sign == -1) {
                    unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
                } else {
                    unscaled = Math.floor(nextAfter(unscaled, Double.NEGATIVE_INFINITY));
                }
                break;
            case BigDecimal.ROUND_HALF_DOWN: {
                unscaled = nextAfter(unscaled, Double.NEGATIVE_INFINITY);
                double fraction = unscaled - Math.floor(unscaled);
                if (fraction > 0.5) {
                    unscaled = Math.ceil(unscaled);
                } else {
                    unscaled = Math.floor(unscaled);
                }
                break;
            }
            case BigDecimal.ROUND_HALF_EVEN: {
                double fraction = unscaled - Math.floor(unscaled);
                if (fraction > 0.5) {
                    unscaled = Math.ceil(unscaled);
                } else if (fraction < 0.5) {
                    unscaled = Math.floor(unscaled);
                } else {
                    // The following equality test is intentional and needed for rounding purposes
                    if (Math.floor(unscaled) / 2.0 == Math.floor(Math
                            .floor(unscaled) / 2.0)) { // even
                        unscaled = Math.floor(unscaled);
                    } else { // odd
                        unscaled = Math.ceil(unscaled);
                    }
                }
                break;
            }
            case BigDecimal.ROUND_HALF_UP: {
                unscaled = nextAfter(unscaled, Double.POSITIVE_INFINITY);
                double fraction = unscaled - Math.floor(unscaled);
                if (fraction >= 0.5) {
                    unscaled = Math.ceil(unscaled);
                } else {
                    unscaled = Math.floor(unscaled);
                }
                break;
            }
            case BigDecimal.ROUND_UNNECESSARY:
                if (unscaled != Math.floor(unscaled)) {
                    throw new ArithmeticException("Inexact result from rounding");
                }
                break;
            case BigDecimal.ROUND_UP:
                unscaled = Math.ceil(nextAfter(unscaled, Double.POSITIVE_INFINITY));
                break;
            default:
                throw MathRuntimeException.createIllegalArgumentException(
                        "invalid rounding method {0}, valid methods: {1} ({2}), {3} ({4})," +
                                " {5} ({6}), {7} ({8}), {9} ({10}), {11} ({12}), {13} ({14}), {15} ({16})",
                        roundingMethod,
                        "ROUND_CEILING", BigDecimal.ROUND_CEILING,
                        "ROUND_DOWN", BigDecimal.ROUND_DOWN,
                        "ROUND_FLOOR", BigDecimal.ROUND_FLOOR,
                        "ROUND_HALF_DOWN", BigDecimal.ROUND_HALF_DOWN,
                        "ROUND_HALF_EVEN", BigDecimal.ROUND_HALF_EVEN,
                        "ROUND_HALF_UP", BigDecimal.ROUND_HALF_UP,
                        "ROUND_UNNECESSARY", BigDecimal.ROUND_UNNECESSARY,
                        "ROUND_UP", BigDecimal.ROUND_UP);
        }
        return unscaled;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for byte value <code>x</code>.
     * <p>
     * For a byte value x, this method returns (byte)(+1) if x > 0, (byte)(0) if
     * x = 0, and (byte)(-1) if x < 0.</p>
     *
     * @param x the value, a byte
     * @return (byte)(+1), (byte)(0), or (byte)(-1), depending on the sign of x
     */
    public static byte sign(final byte x) {
        return (x == ZB) ? ZB : (x > ZB) ? PB : NB;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for double precision <code>x</code>.
     * <p>
     * For a double value <code>x</code>, this method returns
     * <code>+1.0</code> if <code>x > 0</code>, <code>0.0</code> if
     * <code>x = 0.0</code>, and <code>-1.0</code> if <code>x < 0</code>.
     * Returns <code>NaN</code> if <code>x</code> is <code>NaN</code>.</p>
     *
     * @param x the value, a double
     * @return +1.0, 0.0, or -1.0, depending on the sign of x
     */
    public static double sign(final double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }
        return (x == 0.0) ? 0.0 : (x > 0.0) ? 1.0 : -1.0;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for float value <code>x</code>.
     * <p>
     * For a float value x, this method returns +1.0F if x > 0, 0.0F if x =
     * 0.0F, and -1.0F if x < 0. Returns <code>NaN</code> if <code>x</code>
     * is <code>NaN</code>.</p>
     *
     * @param x the value, a float
     * @return +1.0F, 0.0F, or -1.0F, depending on the sign of x
     */
    public static float sign(final float x) {
        if (Float.isNaN(x)) {
            return Float.NaN;
        }
        return (x == 0.0F) ? 0.0F : (x > 0.0F) ? 1.0F : -1.0F;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for int value <code>x</code>.
     * <p>
     * For an int value x, this method returns +1 if x > 0, 0 if x = 0, and -1
     * if x < 0.</p>
     *
     * @param x the value, an int
     * @return +1, 0, or -1, depending on the sign of x
     */
    public static int sign(final int x) {
        return (x == 0) ? 0 : (x > 0) ? 1 : -1;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for long value <code>x</code>.
     * <p>
     * For a long value x, this method returns +1L if x > 0, 0L if x = 0, and
     * -1L if x < 0.</p>
     *
     * @param x the value, a long
     * @return +1L, 0L, or -1L, depending on the sign of x
     */
    public static long sign(final long x) {
        return (x == 0L) ? 0L : (x > 0L) ? 1L : -1L;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/Sign.html"> sign</a>
     * for short value <code>x</code>.
     * <p>
     * For a short value x, this method returns (short)(+1) if x > 0, (short)(0)
     * if x = 0, and (short)(-1) if x < 0.</p>
     *
     * @param x the value, a short
     * @return (short)(+1), (short)(0), or (short)(-1), depending on the sign of
     *         x
     */
    public static short sign(final short x) {
        return (x == ZS) ? ZS : (x > ZS) ? PS : NS;
    }

    /**
     * Returns the <a href="http://mathworld.wolfram.com/HyperbolicSine.html">
     * hyperbolic sine</a> of x.
     *
     * @param x double value for which to find the hyperbolic sine
     * @return hyperbolic sine of x
     */
    public static double sinh(double x) {
        return (Math.exp(x) - Math.exp(-x)) / 2.0;
    }

    /**
     * Subtract two integers, checking for overflow.
     *
     * @param x the minuend
     * @param y the subtrahend
     * @return the difference <code>x-y</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             int
     * @since 1.1
     */
    public static int subAndCheck(int x, int y) {
        long s = (long) x - (long) y;
        if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
            throw new ArithmeticException("overflow: subtract");
        }
        return (int) s;
    }

    /**
     * Subtract two long integers, checking for overflow.
     *
     * @param a first value
     * @param b second value
     * @return the difference <code>a-b</code>
     * @throws ArithmeticException if the result can not be represented as an
     *                             long
     * @since 1.2
     */
    public static long subAndCheck(long a, long b) {
        long ret;
        String msg = "overflow: subtract";
        if (b == Long.MIN_VALUE) {
            if (a < 0) {
                ret = a - b;
            } else {
                throw new ArithmeticException(msg);
            }
        } else {
            // use additive inverse
            ret = addAndCheck(a, -b, msg);
        }
        return ret;
    }

    /**
     * Raise an int to an int power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static int pow(final int k, int e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        int result = 1;
        int k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise an int to a long power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static int pow(final int k, long e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        int result = 1;
        int k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a long to an int power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static long pow(final long k, int e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        long result = 1l;
        long k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a long to a long power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static long pow(final long k, long e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        long result = 1l;
        long k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result *= k2p;
            }
            k2p *= k2p;
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a BigInteger to an int power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static BigInteger pow(final BigInteger k, int e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        return k.pow(e);

    }

    /**
     * Raise a BigInteger to a long power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static BigInteger pow(final BigInteger k, long e)
            throws IllegalArgumentException {

        if (e < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p = k;
        while (e != 0) {
            if ((e & 0x1) != 0) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            e = e >> 1;
        }

        return result;

    }

    /**
     * Raise a BigInteger to a BigInteger power.
     *
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @throws IllegalArgumentException if e is negative
     */
    public static BigInteger pow(final BigInteger k, BigInteger e)
            throws IllegalArgumentException {

        if (e.compareTo(BigInteger.ZERO) < 0) {
            throw MathRuntimeException.createIllegalArgumentException(
                    "cannot raise an integral value to a negative power ({0}^{1})",
                    k, e);
        }

        BigInteger result = BigInteger.ONE;
        BigInteger k2p = k;
        while (!BigInteger.ZERO.equals(e)) {
            if (e.testBit(0)) {
                result = result.multiply(k2p);
            }
            k2p = k2p.multiply(k2p);
            e = e.shiftRight(1);
        }

        return result;

    }

    /**
     * Calculates the L<sub>1</sub> (sum of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>1</sub> distance between the two points
     */
    public static double distance1(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    /**
     * Calculates the L<sub>1</sub> (sum of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>1</sub> distance between the two points
     */
    public static int distance1(int[] p1, int[] p2) {
        int sum = 0;
        for (int i = 0; i < p1.length; i++) {
            sum += Math.abs(p1[i] - p2[i]);
        }
        return sum;
    }

    /**
     * Calculates the L<sub>2</sub> (Euclidean) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>2</sub> distance between the two points
     */
    public static double distance(double[] p1, double[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            final double dp = p1[i] - p2[i];
            sum += dp * dp;
        }
        return Math.sqrt(sum);
    }

    /**
     * Calculates the L<sub>2</sub> (Euclidean) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>2</sub> distance between the two points
     */
    public static double distance(int[] p1, int[] p2) {
        double sum = 0;
        for (int i = 0; i < p1.length; i++) {
            final double dp = p1[i] - p2[i];
            sum += dp * dp;
        }
        return Math.sqrt(sum);
    }

    /**
     * Calculates the L<sub>&infin;</sub> (max of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>&infin;</sub> distance between the two points
     */
    public static double distanceInf(double[] p1, double[] p2) {
        double max = 0;
        for (int i = 0; i < p1.length; i++) {
            max = Math.max(max, Math.abs(p1[i] - p2[i]));
        }
        return max;
    }

    /**
     * Calculates the L<sub>&infin;</sub> (max of abs) distance between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the L<sub>&infin;</sub> distance between the two points
     */
    public static int distanceInf(int[] p1, int[] p2) {
        int max = 0;
        for (int i = 0; i < p1.length; i++) {
            max = Math.max(max, Math.abs(p1[i] - p2[i]));
        }
        return max;
    }

    /**
     * Checks that the given array is sorted.
     *
     * @param val    Values
     * @param dir    Order direction (-1 for decreasing, 1 for increasing)
     * @param strict Whether the order should be strict
     * @throws IllegalArgumentException if the array is not sorted.
     */
    public static void checkOrder(double[] val, int dir, boolean strict) {
        double previous = val[0];

        int max = val.length;
        for (int i = 1; i < max; i++) {
            if (dir > 0) {
                if (strict) {
                    if (val[i] <= previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not strictly increasing ({2} >= {3})",
                                i - 1, i, previous, val[i]);
                    }
                } else {
                    if (val[i] < previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not increasing ({2} > {3})",
                                i - 1, i, previous, val[i]);
                    }
                }
            } else {
                if (strict) {
                    if (val[i] >= previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not strictly decreasing ({2} <= {3})",
                                i - 1, i, previous, val[i]);
                    }
                } else {
                    if (val[i] > previous) {
                        throw MathRuntimeException.createIllegalArgumentException("points {0} and {1} are not decreasing ({2} < {3})",
                                i - 1, i, previous, val[i]);
                    }
                }
            }

            previous = val[i];
        }
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

