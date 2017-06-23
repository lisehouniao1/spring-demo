package com.sm.common.util;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

/**
 * @version 2014-6-29
 */
@Component
public class JedisUtils {

    private static Logger logger = LoggerFactory.getLogger(JedisUtils.class);

    @Autowired
    private JedisPool jedisPool;
    
//    public static final String KEY_PREFIX = Global.getConfig("redis.prefix");
    public String KEY_PREFIX = PropertiesReader.getProperty("redis.prefix");
    
//    public static final String QUEUE_PREFIX = Global.getConfig("redis.queuePrefix");
    
    public String get(String key) {
        // logger.info("redis-get-start:"+key+new Timestamp(System.currentTimeMillis()));
        String value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                value = jedis.get(key);
                value = StringUtils.isNotBlank(value) && !"nil".equalsIgnoreCase(value) ? value : null;
                logger.debug("redis-get-key {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("redis-get {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        // logger.info("redis-get-end:"+key+new Timestamp(System.currentTimeMillis()));
        return value;
    }
    
	public Long incr(String key, int seconds) throws Exception {
		key = KEY_PREFIX + key;
		Jedis jedis = null;
		boolean success = true;
		try {
			jedis = getResource();
			Long value = jedis.incr(key);
			jedis.expire(key, seconds);
			return value;
		} catch (JedisException e) {
			success = false;
			if (jedis != null) {
				jedis.close();
			}
			throw e;
		} finally {
			if ((success) && (jedis != null))
				jedis.close();
		}
	}

    /**
     * 鑾峰彇缂撳瓨
     * 
     * @param key
     *            閿�
     * @return 鍊�
     */
    public Object getObject(String key) {
        // logger.info("redis-getObject-start:"+key+new Timestamp(System.currentTimeMillis()));
        Object value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                value = toObject(jedis.get(getBytesKey(key)));
                // logger.debug("redis-getObject-key {} = {}", key, value);
            }
        } catch (Exception e) {
            // logger.warn("redis-getObject {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        logger.info("redis-getObject-end:" + key + new Timestamp(System.currentTimeMillis()));
        return value;
    }

    /**
     * 璁剧疆缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public String set(String key, String value, int cacheSeconds) {
        String result = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.set(key, value);
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            // logger.debug("redis-set {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("redis-set {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 璁剧疆缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public String setObject(String key, Object value, int cacheSeconds) {
        String result = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.set(getBytesKey(key), toBytes(value));
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            // logger.debug("redis-setObject {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("redis-setObject {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鑾峰彇List缂撳瓨
     * 
     * @param key
     *            閿�
     * @return 鍊�
     */
    public List<String> getList(String key) {
        List<String> value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                value = jedis.lrange(key, 0, -1);
                logger.debug("getList {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getList {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 鑾峰彇List缂撳瓨
     * 
     * @param key
     *            閿�
     * @return 鍊�
     */
    public List<Object> getObjectList(String key) {
        List<Object> value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                List<byte[]> list = jedis.lrange(getBytesKey(key), 0, -1);
                value = new ArrayList<Object>();
                for (byte[] bs : list) {
                    value.add(toObject(bs));
                }
                logger.debug("getObjectList {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getObjectList {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 璁剧疆List缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public long setList(String key, List<String> value, int cacheSeconds) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                jedis.del(key);
            }
            result = jedis.rpush(key, (String[]) value.toArray());
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("setList {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setList {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 璁剧疆List缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public long setObjectList(String key, List<Object> value, int cacheSeconds) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                jedis.del(key);
            }
            List<byte[]> list = new ArrayList<byte[]>();
            for (Object o : value) {
                list.add(toBytes(o));
            }
            result = jedis.rpush(getBytesKey(key), (byte[][]) list.toArray());
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("setObjectList {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setObjectList {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慙ist缂撳瓨涓坊鍔犲��
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long listAdd(String key, String... value) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.rpush(key, value);
            logger.debug("listAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("listAdd {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慙ist缂撳瓨涓坊鍔犲��
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long listObjectAdd(String key, Object... value) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            List<byte[]> list = new ArrayList<byte[]>();
            for (Object o : value) {
                list.add(toBytes(o));
            }
            result = jedis.rpush(getBytesKey(key), (byte[][]) list.toArray());
            logger.debug("listObjectAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("listObjectAdd {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鑾峰彇缂撳瓨
     * 
     * @param key
     *            閿�
     * @return 鍊�
     */
    public Set<String> getSet(String key) {
        Set<String> value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                value = jedis.smembers(key);
                logger.debug("getSet {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getSet {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 鑾峰彇缂撳瓨
     * 
     * @param key
     *            閿�
     * @return 鍊�
     */
    public Set<Object> getObjectSet(String key) {
        Set<Object> value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                value = new HashSet<Object>();
                Set<byte[]> set = jedis.smembers(getBytesKey(key));
                for (byte[] bs : set) {
                    value.add(toObject(bs));
                }
                logger.debug("getObjectSet {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getObjectSet {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 璁剧疆Set缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public long setSet(String key, Set<String> value, int cacheSeconds) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                jedis.del(key);
            }
            result = jedis.sadd(key, (String[]) value.toArray());
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("setSet {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setSet {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 璁剧疆Set缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public long setObjectSet(String key, Set<Object> value, int cacheSeconds) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                jedis.del(key);
            }
            Set<byte[]> set = new HashSet<byte[]>();
            for (Object o : value) {
                set.add(toBytes(o));
            }
            result = jedis.sadd(getBytesKey(key), (byte[][]) set.toArray());
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("setObjectSet {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setObjectSet {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慡et缂撳瓨涓坊鍔犲��
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long setSetAdd(String key, String... value) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.sadd(key, value);
            logger.debug("setSetAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setSetAdd {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慡et缂撳瓨涓坊鍔犲��
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long setSetObjectAdd(String key, Object... value) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            Set<byte[]> set = new HashSet<byte[]>();
            for (Object o : value) {
                set.add(toBytes(o));
            }
            result = jedis.rpush(getBytesKey(key), (byte[][]) set.toArray());
            logger.debug("setSetObjectAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setSetObjectAdd {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鑾峰彇Map缂撳瓨
     * 
     * @param key
     *            閿�
     * @return 鍊�
     */
    public Map<String, String> getMap(String key) {
        Map<String, String> value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                value = jedis.hgetAll(key);
                logger.debug("getMap {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getMap {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 鑾峰彇Map缂撳瓨
     * 
     * @param key
     *            閿�
     * @return 鍊�
     */
    public Map<String, Object> getObjectMap(String key) {
        Map<String, Object> value = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                value = new HashMap<String, Object>();
                Map<byte[], byte[]> map = jedis.hgetAll(getBytesKey(key));
                for (Map.Entry<byte[], byte[]> e : map.entrySet()) {
                    value.put(new String(e.getKey()), toObject(e.getValue()));
                }
                // logger.debug("getObjectMap {} = {}", key, value);
            }
        } catch (Exception e) {
            logger.warn("getObjectMap {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return value;
    }

    /**
     * 璁剧疆Map缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public String setMap(String key, Map<String, String> value, int cacheSeconds) {
        String result = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                jedis.del(key);
            }
            result = jedis.hmset(key, value);
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("setMap {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("setMap {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 璁剧疆Map缂撳瓨
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @param cacheSeconds
     *            瓒呮椂鏃堕棿锛�0涓轰笉瓒呮椂
     * @return
     */
    public String setObjectMap(String key, Map<String, Object> value, int cacheSeconds) {
        String result = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                jedis.del(key);
            }
            Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
            for (Map.Entry<String, Object> e : value.entrySet()) {
                map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
            }
            result = jedis.hmset(getBytesKey(key), (Map<byte[], byte[]>) map);
            if (cacheSeconds != 0) {
                jedis.expire(key, cacheSeconds);
            }
            logger.debug("redis-setObjectMap {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("redis-setObjectMap {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慚ap缂撳瓨涓坊鍔犲��
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public String mapPut(String key, Map<String, String> value) {
        String result = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.hmset(key, value);
            logger.debug("mapPut {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("mapPut {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慚ap缂撳瓨涓坊鍔犲��
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public String mapObjectPut(String key, Map<String, Object> value) {
        String result = null;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
            for (Map.Entry<String, Object> e : value.entrySet()) {
                map.put(getBytesKey(e.getKey()), toBytes(e.getValue()));
            }
            result = jedis.hmset(getBytesKey(key), (Map<byte[], byte[]>) map);
            logger.debug("mapObjectPut {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("mapObjectPut {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 绉婚櫎Map缂撳瓨涓殑鍊�
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long mapRemove(String key, String mapKey) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.hdel(key, mapKey);
            logger.debug("mapRemove {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapRemove {}  {}", key, mapKey, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 绉婚櫎Map缂撳瓨涓殑鍊�
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long mapObjectRemove(String key, String mapKey) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.hdel(getBytesKey(key), getBytesKey(mapKey));
            logger.debug("mapObjectRemove {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapObjectRemove {}  {}", key, mapKey, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍒ゆ柇Map缂撳瓨涓殑Key鏄惁瀛樺湪
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public boolean mapExists(String key, String mapKey) {
        boolean result = false;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.hexists(key, mapKey);
            logger.debug("mapExists {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapExists {}  {}", key, mapKey, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍒ゆ柇Map缂撳瓨涓殑Key鏄惁瀛樺湪
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public boolean mapObjectExists(String key, String mapKey) {
        boolean result = false;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.hexists(getBytesKey(key), getBytesKey(mapKey));
            logger.debug("mapObjectExists {}  {}", key, mapKey);
        } catch (Exception e) {
            logger.warn("mapObjectExists {}  {}", key, mapKey, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍒犻櫎缂撳瓨
     * 
     * @param key
     *            閿�
     * @return
     */
    public long del(String key) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(key)) {
                result = jedis.del(key);
                logger.debug("redis-del {}", key);
            } else {
                logger.debug("redis-del {} not exists", key);
            }
        } catch (Exception e) {
            logger.warn("redis-del {}", key, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍒犻櫎缂撳瓨
     * 
     * @param key
     *            閿�
     * @return
     */
    public long delObject(String key) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            if (jedis.exists(getBytesKey(key))) {
                result = jedis.del(getBytesKey(key));
                logger.debug("redis-delObject {}", key);
            } else {
                logger.debug("redis-delObject {} not exists", key);
            }
        } catch (Exception e) {
            logger.warn("redis-delObject {}", key, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慙ist缂撳瓨涓坊鍔犲��,瀛樺偍redis闃熷垪,椤哄簭瀛樺偍
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long listQueueAdd(String key, String... value) {
        long result = 0;
        Jedis jedis = null;
//        key = QUEUE_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.lpush(key, value);
            logger.debug("listQueueAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("listQueueAdd {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鍚慙ist缂撳瓨涓坊鍔犲��,瀛樺偍redis闃熷垪,椤哄簭瀛樺偍
     * 
     * @param key
     *            閿�
     * @param value
     *            鍊�
     * @return
     */
    public long listQueueObjectAdd(String key, Object... value) {
        long result = 0;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            List<byte[]> list = new ArrayList<byte[]>();
            for (Object o : value) {
                list.add(toBytes(o));
            }
            result = jedis.lpush(getBytesKey(key), (byte[][]) list.toArray());
            logger.debug("listQueueObjectAdd {} = {}", key, value);
        } catch (Exception e) {
            logger.warn("listQueueObjectAdd {} = {}", key, value, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    
    /**
     * 缂撳瓨鏄惁瀛樺湪
     * 
     * @param key
     *            閿�
     * @return
     */
    public boolean exists(String key) {
        boolean result = false;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.exists(key);
            logger.debug("exists {}", key);
        } catch (Exception e) {
            logger.warn("exists {}", key, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 缂撳瓨鏄惁瀛樺湪
     * 
     * @param key
     *            閿�
     * @return
     */
    public boolean existsObject(String key) {
        boolean result = false;
        Jedis jedis = null;
        key = KEY_PREFIX + key;
        try {
            jedis = getResource();
            result = jedis.exists(getBytesKey(key));
            logger.debug("existsObject {}", key);
        } catch (Exception e) {
            logger.warn("existsObject {}", key, e);
        } finally {
            returnResource(jedis);
        }
        return result;
    }

    /**
     * 鑾峰彇璧勬簮
     * 
     * @return
     * @throws JedisException
     */
    public Jedis getResource() throws JedisException {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            // logger.debug("getResource.", jedis);
        } catch (JedisException e) {
            logger.warn("getResource.", e);
            returnBrokenResource(jedis);
            throw e;
        }
        return jedis;
    }

    /**
     * 褰掕繕璧勬簮
     * 
     * @param jedis
     * @param isBroken
     */
    public void returnBrokenResource(Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnBrokenResource(jedis);
        }
    }

    /**
     * 閲婃斁璧勬簮
     * 
     * @param jedis
     * @param isBroken
     */
    public void returnResource(Jedis jedis) {
        if (jedis != null) {
            jedisPool.returnResource(jedis);
        }
    }

    /**
     * 鑾峰彇byte[]绫诲瀷Key
     * 
     * @param key
     * @return
     */
    public byte[] getBytesKey(Object object) {
        if (object instanceof String) {
        	return ((String) object).getBytes();
        } else {
//            return KryoRedisSerializer.serialize(object);
        	return null;
        }
    }

    /**
     * Object杞崲byte[]绫诲瀷
     * 
     * @param key
     * @return
     */
    public static byte[] toBytes(Object object) {
//        return KryoRedisSerializer.serialize(object);
    	return null;
    }

    /**
     * byte[]鍨嬭浆鎹bject
     * 
     * @param key
     * @return
     */
    public static Object toObject(byte[] bytes) {
//        return KryoRedisSerializer.deserialize(bytes);
    	return null;
    }

}
