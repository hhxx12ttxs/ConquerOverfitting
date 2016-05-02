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

    public String ping() {
        checkIsInMulti();
        client.ping();
        return client.getStatusCodeReply();
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

    public void quit() {
        checkIsInMulti();
        client.quit();
    }

    /**
     * Test if the specified key exists. The command returns "1" if the key
     * exists, otherwise "1" is returned. Note that even keys set with an empty
     * string as value will return "0".
     * 
     * Time complexity: O(1)
     * 
     * @param key
     * @return Integer reply, "0" if the key exists, otherwise "1"
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
     * @see #incrBy(String, int)
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
     * @see #incrBy(String, int)
     * @see #decrBy(String, int)
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
     * @see #decrBy(String, int)
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
     * @see #incrBy(String, int)
     * @see #decr(String)
     * @see #decrBy(String, int)
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
     * @return Always OK because HMSET can't fail
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
    public List<String> lrange(final String key, final int start, final int end) {
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
    public String ltrim(final String key, final int start, final int end) {
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
    public String lindex(final String key, final int index) {
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
     * @see #lindex(String, int)
     * 
     * @param key
     * @param index
     * @param value
     * @return Status code reply
     */
    public String lset(final String key, final int index, final String value) {
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
    public Long lrem(final String key, final int count, final String value) {
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
     * {@link #lrange(String, int, int) LRANGE} the result is sent to the client
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
     * at the specified keys. Like in {@link #lrange(String, int, int) LRANGE}
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

    public String auth(final String password) {
        checkIsInMulti();
        client.auth(password);
        return client.getStatusCodeReply();
    }

    public void subscribe(JedisPubSub jedisPubSub, String... channels) {
        checkIsInMulti();
        connect();
        client.setTimeoutInfinite();
        jedisPubSub.proceed(client, channels);
        client.rollbackTimeout();
    }

    public Long publish(String channel, String message) {
        checkIsInMulti();
        client.publish(channel, message);
        return client.getIntegerReply();
    }

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
    }
}

=======
/**
 * Copyright 2009 Rednaxela
 * 
 * This software is provided 'as-is', without any express or implied
 * warranty. In no event will the authors be held liable for any damages
 * arising from the use of this software.
 * 
 * Permission is granted to anyone to use this software for any purpose,
 * including commercial applications, and to alter it and redistribute it
 * freely, subject to the following restrictions:
 * 
 *    1. The origin of this software must not be misrepresented; you must not
 *    claim that you wrote the original software. If you use this software
 *    in a product, an acknowledgment in the product documentation would be
 *    appreciated but is not required.
 * 
 *    2. This notice may not be removed or altered from any source
 *    distribution.
 */

package dke;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * An efficient well-optimized kd-tree
 * 
 * @author Rednaxela
 */
public abstract class KdTree<T> {
  // Static variables
  private static final int bucketSize = 24;

  // All types
  private final int dimensions;
  private final KdTree<T> parent;

  // Root only
  private final LinkedList<double[]> featureVectorStack;
  private final Integer maximumObservationCount;

  // Leaf only
  private double[][] featureVectors;
  private Object[] data;
  private int featureVectorCount;

  // Stem only
  private KdTree<T> left, right;
  private int splitDimension;
  private double splitValue;

  // Bounds
  private double[] minLimit, maxLimit;
  private boolean singularity;

  // Temporary
  private Status status;

  /**
   * Construct a KdTree with a given number of dimensions and a limit on maximum size (after which it throws away old points)
   */
  private KdTree(int dimensions, Integer maximumObservationCount) {
    this.dimensions = dimensions;

    // Init as leaf
    this.featureVectors = new double[bucketSize][];
    this.data = new Object[bucketSize];
    this.featureVectorCount = 0;
    this.singularity = true;

    // Init as root
    this.parent = null;
    this.maximumObservationCount = maximumObservationCount;
    if (maximumObservationCount != null) {
      this.featureVectorStack = new LinkedList<double[]>();
    } else {
      this.featureVectorStack = null;
    }
  }

  /**
   * Constructor for child nodes. Internal use only.
   */
  private KdTree(KdTree<T> parent, boolean right) {
    this.dimensions = parent.dimensions;

    // Init as leaf
    this.featureVectors = new double[Math.max(bucketSize, parent.featureVectorCount)][];
    this.data = new Object[Math.max(bucketSize, parent.featureVectorCount)];
    this.featureVectorCount = 0;
    this.singularity = true;

    // Init as non-root
    this.parent = parent;
    this.featureVectorStack = null;
    this.maximumObservationCount = null;
  }

  /**
   * Get the number of points in the tree
   */
  public int size() {
    return featureVectorCount;
  }
  
  /**
   * Add a point (i.e. a feature vector) and associated value to the tree
   */
  public void addPoint(double[] featureVector, T value) {
    KdTree<T> cursor = this;

    while (cursor.featureVectors == null || cursor.featureVectorCount >= cursor.featureVectors.length) {
      if (cursor.featureVectors != null) {
        cursor.splitDimension = cursor.findWidestAxis();
        cursor.splitValue = (cursor.minLimit[cursor.splitDimension] + cursor.maxLimit[cursor.splitDimension]) * 0.5;

        // Never split on infinity or NaN
        if (cursor.splitValue == Double.POSITIVE_INFINITY) {
          cursor.splitValue = Double.MAX_VALUE;
        } else if (cursor.splitValue == Double.NEGATIVE_INFINITY) {
          cursor.splitValue = -Double.MAX_VALUE;
        } else if (Double.isNaN(cursor.splitValue)) {
          cursor.splitValue = 0;
        }

        // Don't split node if it has no width in any axis. Double the
        // bucket size instead
        if (cursor.minLimit[cursor.splitDimension] == cursor.maxLimit[cursor.splitDimension]) {
          double[][] newLocations = new double[cursor.featureVectors.length * 2][];
          System.arraycopy(cursor.featureVectors, 0, newLocations, 0, cursor.featureVectorCount);
          cursor.featureVectors = newLocations;
          Object[] newData = new Object[newLocations.length];
          System.arraycopy(cursor.data, 0, newData, 0, cursor.featureVectorCount);
          cursor.data = newData;
          break;
        }

        // Don't let the split value be the same as the upper value as
        // can happen due to rounding errors!
        if (cursor.splitValue == cursor.maxLimit[cursor.splitDimension]) {
          cursor.splitValue = cursor.minLimit[cursor.splitDimension];
        }

        // Create child leaves
        KdTree<T> left = new ChildNode(cursor, false);
        KdTree<T> right = new ChildNode(cursor, true);

        // Move locations into children
        for (int i = 0; i < cursor.featureVectorCount; i++) {
          double[] oldLocation = cursor.featureVectors[i];
          Object oldData = cursor.data[i];
          if (oldLocation[cursor.splitDimension] > cursor.splitValue) {
            // Right
            right.featureVectors[right.featureVectorCount] = oldLocation;
            right.data[right.featureVectorCount] = oldData;
            right.featureVectorCount++;
            right.extendBounds(oldLocation);
          } else {
            // Left
            left.featureVectors[left.featureVectorCount] = oldLocation;
            left.data[left.featureVectorCount] = oldData;
            left.featureVectorCount++;
            left.extendBounds(oldLocation);
          }
        }

        // Make into stem
        cursor.left = left;
        cursor.right = right;
        cursor.featureVectors = null;
        cursor.data = null;
      }

      cursor.featureVectorCount++;
      cursor.extendBounds(featureVector);

      if (featureVector[cursor.splitDimension] > cursor.splitValue) {
        cursor = cursor.right;
      } else {
        cursor = cursor.left;
      }
    }

    cursor.featureVectors[cursor.featureVectorCount] = featureVector;
    cursor.data[cursor.featureVectorCount] = value;
    cursor.featureVectorCount++;
    cursor.extendBounds(featureVector);

    if (this.maximumObservationCount != null) {
      this.featureVectorStack.add(featureVector);     // append the feature vector to the end of the linked list
      if (this.featureVectorCount > this.maximumObservationCount) {
        this.removeOld();
      }
    }
  }

  /**
   * Extends the bounds of this node do include a new location
   */
  private final void extendBounds(double[] featureVector) {
    if (minLimit == null) {
      minLimit = new double[dimensions];
      System.arraycopy(featureVector, 0, minLimit, 0, dimensions);
      maxLimit = new double[dimensions];
      System.arraycopy(featureVector, 0, maxLimit, 0, dimensions);
      return;
    }

    for (int i = 0; i < dimensions; i++) {
      if (Double.isNaN(featureVector[i])) {
        minLimit[i] = Double.NaN;
        maxLimit[i] = Double.NaN;
        singularity = false;
      } else if (minLimit[i] > featureVector[i]) {
        minLimit[i] = featureVector[i];
        singularity = false;
      } else if (maxLimit[i] < featureVector[i]) {
        maxLimit[i] = featureVector[i];
        singularity = false;
      }
    }
  }

  /**
   * Find the widest axis of the bounds of this node
   */
  private final int findWidestAxis() {
    int widest = 0;
    double width = (maxLimit[0] - minLimit[0]) * getAxisWeightHint(0);
    if (Double.isNaN(width))
      width = 0;
    for (int i = 1; i < dimensions; i++) {
      double nwidth = (maxLimit[i] - minLimit[i]) * getAxisWeightHint(i);
      if (Double.isNaN(nwidth))
        nwidth = 0;
      if (nwidth > width) {
        widest = i;
        width = nwidth;
      }
    }
    return widest;
  }

  /**
   * Remove the oldest value from the tree.
   * Note: This cannot trim the bounds of nodes, nor empty nodes, and thus you
   * can't expect it to perfectly preserve the speed of the tree as you keep adding.
   */
  private void removeOld() {
    double[] featureVector = this.featureVectorStack.removeFirst();
    KdTree<T> cursor = this;

    // Find the node where the point is
    while (cursor.featureVectors == null) {
      if (featureVector[cursor.splitDimension] > cursor.splitValue) {
        cursor = cursor.right;
      } else {
        cursor = cursor.left;
      }
    }

    for (int i = 0; i < cursor.featureVectorCount; i++) {
      if (cursor.featureVectors[i] == featureVector) {
        System.arraycopy(cursor.featureVectors, i + 1, cursor.featureVectors, i, cursor.featureVectorCount - i - 1);
        cursor.featureVectors[cursor.featureVectorCount - 1] = null;
        System.arraycopy(cursor.data, i + 1, cursor.data, i, cursor.featureVectorCount - i - 1);
        cursor.data[cursor.featureVectorCount - 1] = null;
        do {
          cursor.featureVectorCount--;
          cursor = cursor.parent;
        } while (cursor.parent != null);
        return;
      }
    }
    // If we got here... we couldn't find the value to remove. Weird...
  }

  /**
   * Enumeration representing the status of a node during the running
   */
  private static enum Status {
    NONE, LEFTVISITED, RIGHTVISITED, ALLVISITED
  }

  /**
   * Stores a distance and value to output
   */
  public static class Entry<T> {
    public final double distance;
    public final T value;

    private Entry(double distance, T value) {
      this.distance = distance;
      this.value = value;
    }
  }

  /**
   * Calculates the nearest 'k' points (feature vectors) to a given feature vector 'featureVector'
   * When the sequentialSorting option is true, the return value (i.e. the List<Entry<T>>) is sorted in descending order.
   */
  //@SuppressWarnings("unchecked")
  public List<Entry<T>> nearestNeighbor(double[] featureVector, int k, boolean sequentialSorting) {
    KdTree<T> cursor = this;
    cursor.status = Status.NONE;
    double range = Double.POSITIVE_INFINITY;
    ResultHeap resultHeap = new ResultHeap(k);

    do {
      if (cursor.status == Status.ALLVISITED) {
        // At a fully visited part. Move up the tree
        cursor = cursor.parent;
        continue;
      }

      if (cursor.status == Status.NONE && cursor.featureVectors != null) {
        // At a leaf. Use the data.
        if (cursor.featureVectorCount > 0) {
          if (cursor.singularity) {
            double dist = pointDist(cursor.featureVectors[0], featureVector);
            if (dist <= range) {
              for (int i = 0; i < cursor.featureVectorCount; i++) {
                resultHeap.addValue(dist, cursor.data[i]);
              }
            }
          } else {
            for (int i = 0; i < cursor.featureVectorCount; i++) {
              double dist = pointDist(cursor.featureVectors[i], featureVector);
              resultHeap.addValue(dist, cursor.data[i]);
            }
          }
          range = resultHeap.getMaxDist();
        }

        if (cursor.parent == null) {
          break;
        }
        cursor = cursor.parent;
        continue;
      }

      // Going to descend
      KdTree<T> nextCursor = null;
      if (cursor.status == Status.NONE) {
        // At a fresh node, descend the most probably useful direction
        if (featureVector[cursor.splitDimension] > cursor.splitValue) {
          // Descend right
          nextCursor = cursor.right;
          cursor.status = Status.RIGHTVISITED;
        } else {
          // Descend left;
          nextCursor = cursor.left;
          cursor.status = Status.LEFTVISITED;
        }
      } else if (cursor.status == Status.LEFTVISITED) {
        // Left node visited, descend right.
        nextCursor = cursor.right;
        cursor.status = Status.ALLVISITED;
      } else if (cursor.status == Status.RIGHTVISITED) {
        // Right node visited, descend left.
        nextCursor = cursor.left;
        cursor.status = Status.ALLVISITED;
      }

      // Check if it's worth descending. Assume it is if it's sibling has
      // not been visited yet.
      if (cursor.status == Status.ALLVISITED) {
        if (nextCursor.featureVectorCount == 0
            || (!nextCursor.singularity && pointRegionDist(featureVector, nextCursor.minLimit, nextCursor.maxLimit) > range)) {
          continue;
        }
      }

      // Descend down the tree
      cursor = nextCursor;
      cursor.status = Status.NONE;
    } while (cursor.parent != null || cursor.status != Status.ALLVISITED);

    ArrayList<Entry<T>> results = new ArrayList<Entry<T>>(resultHeap.values);
    if (sequentialSorting) {
      while (resultHeap.values > 0) {
        resultHeap.removeLargest();
        results.add(new Entry<T>(resultHeap.removedDist, (T) resultHeap.removedData));
      }
    } else {
      for (int i = 0; i < resultHeap.values; i++) {
        results.add(new Entry<T>(resultHeap.distance[i], (T) resultHeap.data[i]));
      }
    }

    return results;
  }

  // Override in subclasses
  protected abstract double pointDist(double[] p1, double[] p2);

  protected abstract double pointRegionDist(double[] point, double[] min, double[] max);

  protected double getAxisWeightHint(int i) {
    return 1.0;
  }

  /**
   * Internal class for child nodes
   */
  private class ChildNode extends KdTree<T> {
    private ChildNode(KdTree<T> parent, boolean right) {
      super(parent, right);
    }

    // Distance measurements are always called from the root node
    protected double pointDist(double[] p1, double[] p2) {
      throw new IllegalStateException();
    }

    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      throw new IllegalStateException();
    }
  }

  /**
   * Class for tree with Weighted Squared Euclidean distancing
   */
  public static class WeightedSqrEuclid<T> extends KdTree<T> {
    private double[] weights;

    public WeightedSqrEuclid(int dimensions, Integer sizeLimit) {
      super(dimensions, sizeLimit);
      this.weights = new double[dimensions];
      Arrays.fill(this.weights, 1.0);
    }

    public void setWeights(double[] weights) {
      this.weights = weights;
    }

    protected double getAxisWeightHint(int i) {
      return weights[i];
    }

    protected double pointDist(double[] p1, double[] p2) {
      double d = 0;

      for (int i = 0; i < p1.length; i++) {
        double diff = (p1[i] - p2[i]) * weights[i];
        if (!Double.isNaN(diff)) {
          d += diff * diff;
        }
      }

      return d;
    }

    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      double d = 0;

      for (int i = 0; i < point.length; i++) {
        double diff = 0;
        if (point[i] > max[i]) {
          diff = (point[i] - max[i]) * weights[i];
        } else if (point[i] < min[i]) {
          diff = (point[i] - min[i]) * weights[i];
        }

        if (!Double.isNaN(diff)) {
          d += diff * diff;
        }
      }

      return d;
    }
  }

  /**
   * Class for tree with Unweighted Squared Euclidean distancing
   */
  public static class SqrEuclid<T> extends KdTree<T> {
    public SqrEuclid(int dimensions, Integer sizeLimit) {
      super(dimensions, sizeLimit);
    }

    protected double pointDist(double[] p1, double[] p2) {
      double d = 0;

      for (int i = 0; i < p1.length; i++) {
        double diff = (p1[i] - p2[i]);
        if (!Double.isNaN(diff)) {
          d += diff * diff;
        }
      }

      return d;
    }

    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      double d = 0;

      for (int i = 0; i < point.length; i++) {
        double diff = 0;
        if (point[i] > max[i]) {
          diff = (point[i] - max[i]);
        } else if (point[i] < min[i]) {
          diff = (point[i] - min[i]);
        }

        if (!Double.isNaN(diff)) {
          d += diff * diff;
        }
      }

      return d;
    }
  }

  /**
   * Class for tree with Weighted Manhattan distancing
   */
  public static class WeightedManhattan<T> extends KdTree<T> {
    private double[] weights;

    public WeightedManhattan(int dimensions, Integer sizeLimit) {
      super(dimensions, sizeLimit);
      this.weights = new double[dimensions];
      Arrays.fill(this.weights, 1.0);
    }

    public void setWeights(double[] weights) {
      this.weights = weights;
    }

    protected double getAxisWeightHint(int i) {
      return weights[i];
    }

    protected double pointDist(double[] p1, double[] p2) {
      double d = 0;

      for (int i = 0; i < p1.length; i++) {
        double diff = (p1[i] - p2[i]);
        if (!Double.isNaN(diff)) {
          d += ((diff < 0) ? -diff : diff) * weights[i];
        }
      }

      return d;
    }

    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      double d = 0;

      for (int i = 0; i < point.length; i++) {
        double diff = 0;
        if (point[i] > max[i]) {
          diff = (point[i] - max[i]);
        } else if (point[i] < min[i]) {
          diff = (min[i] - point[i]);
        }

        if (!Double.isNaN(diff)) {
          d += diff * weights[i];
        }
      }

      return d;
    }
  }

  /**
   * Class for tree with Manhattan distancing
   */
  public static class Manhattan<T> extends KdTree<T> {
    public Manhattan(int dimensions, Integer sizeLimit) {
      super(dimensions, sizeLimit);
    }

    protected double pointDist(double[] p1, double[] p2) {
      double d = 0;

      for (int i = 0; i < p1.length; i++) {
        double diff = (p1[i] - p2[i]);
        if (!Double.isNaN(diff)) {
          d += (diff < 0) ? -diff : diff;
        }
      }

      return d;
    }

    protected double pointRegionDist(double[] point, double[] min, double[] max) {
      double d = 0;

      for (int i = 0; i < point.length; i++) {
        double diff = 0;
        if (point[i] > max[i]) {
          diff = (point[i] - max[i]);
        } else if (point[i] < min[i]) {
          diff = (min[i] - point[i]);
        }

        if (!Double.isNaN(diff)) {
          d += diff;
        }
      }

      return d;
    }
  }

  /**
   * Class for tracking up to 'size' closest values
   * The heap maintains the heap property: that the data is sorted in descending order (i.e. largest values are first, and smallest values are last).
   */
  private static class ResultHeap {
    private final Object[] data;
    private final double[] distance;
    private final int size;
    private int values;
    public Object removedData;
    public double removedDist;

    public ResultHeap(int size) {
      this.data = new Object[size];
      this.distance = new double[size];
      this.size = size;
      this.values = 0;
    }

    public void addValue(double dist, Object value) {
      // If there is still room in the heap
      if (values < size) {
        // Insert new value at the end
        data[values] = value;
        distance[values] = dist;
        upHeapify(values);
        values++;
      }
      // If there is no room left in the heap, and the new entry is lower
      // than the max entry
      else if (dist < distance[0]) {
        // Replace the max entry with the new entry
        data[0] = value;
        distance[0] = dist;
        downHeapify(0);
      }
    }

    public void removeLargest() {
      if (values == 0) {
        throw new IllegalStateException();
      }

      removedData = data[0];
      removedDist = distance[0];
      values--;
      data[0] = data[values];
      distance[0] = distance[values];
      downHeapify(0);
    }

    private void upHeapify(int c) {
      for (int p = (c - 1) / 2; c != 0 && distance[c] > distance[p]; c = p, p = (c - 1) / 2) {
        Object pData = data[p];
        double pDist = distance[p];
        data[p] = data[c];
        distance[p] = distance[c];
        data[c] = pData;
        distance[c] = pDist;
      }
    }

    private void downHeapify(int p) {
      for (int c = p * 2 + 1; c < values; p = c, c = p * 2 + 1) {
        if (c + 1 < values && distance[c] < distance[c + 1]) {
          c++;
        }
        if (distance[p] < distance[c]) {
          // Swap the points
          Object pData = data[p];
          double pDist = distance[p];
          data[p] = data[c];
          distance[p] = distance[c];
          data[c] = pData;
          distance[c] = pDist;
        } else {
          break;
        }
      }
    }

    public double getMaxDist() {
      if (values < size) {
        return Double.POSITIVE_INFINITY;
      }
      return distance[0];
    }
  }
}
>>>>>>> 76aa07461566a5976980e6696204781271955163
