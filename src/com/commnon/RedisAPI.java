package com.commnon;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisAPI {
    private static JedisPool pool = null;
    private static ThreadLocal<JedisPool> poolThreadLocal = new ThreadLocal<JedisPool>();

    /**
     * 构建redis连接池
     * 
     * @param ip
     * @param port
     * @return JedisPool
     */
    public static JedisPool getPool() {
        if (pool == null) {
            ResourceBundle bundle = ResourceBundle.getBundle("redis");
            if (bundle == null) {
                throw new IllegalArgumentException(
                        "[redis.properties] is not found!");
            }
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(Integer.valueOf(bundle
                    .getString("redis.pool.maxTotal")));
            config.setMaxIdle(Integer.valueOf(bundle
                    .getString("redis.pool.maxIdle")));
            config.setMaxWaitMillis(Long.valueOf(bundle.getString("redis.pool.maxWaitMillis")));
            config.setTestOnBorrow(Boolean.valueOf(bundle
                    .getString("redis.pool.testOnBorrow")));
            config.setTestOnReturn(Boolean.valueOf(bundle
                    .getString("redis.pool.testOnReturn")));
//            TestOnBorrow - Sends a PING request when you ask for the resource.
//            TestOnReturn - Sends a PING whe you return a resource to the pool
            pool = new JedisPool(config, bundle.getString("redis.ip"),
                    Integer.valueOf(bundle.getString("redis.port")));
        }
        return pool;
    }
    
    public static JedisPool getConnection() {
        // ②如果poolThreadLocal没有本线程对应的JedisPool创建一个新的JedisPool，将其保存到线程本地变量中。
        if (poolThreadLocal.get() == null) {
            pool = RedisAPI.getPool();
            poolThreadLocal.set(pool);
            return pool;
        } else {
            return poolThreadLocal.get();// ③直接返回线程本地变量
        }
    }

    /**
     * 返还到连接池
     * 
     * @param pool
     * @param redis
     */
    public static void returnResource(JedisPool pool, Jedis redis) {
        if (redis != null) {
            pool.returnResource(redis);
        }
    }

    /**
     * String operation : get 
     * 
     * @param key
     * @return
     */
    public static String get(String key) {
        String value = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return value;
    }

    /**
     * String operation : set 
     * 
     * @param key
     * @return
     */
    public static String set(String key, String value) {
        String result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }
    //----------------------------------- list operation ------------------
    /**
     * set a value at index in the list.
     * 
     * @param key
     * @return
     */
    public static String lset(String listName, int index, String value) {
        String result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.lset(listName, index, value);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            returnResource(pool, jedis);
        }
        return result;
    }
    /**
     * push a value to the list.
     * 
     * @param key
     * @return
     */
    public static Long lpush(String listName, String value) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.lpush(listName, value);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            returnResource(pool, jedis);
        }
        return result;
    }
    /**
     * get a value at index in the list.
     * 
     * @param key
     * @return
     */
    public static String lindex(String listName, int index) {
        String result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.lindex(listName, index);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            returnResource(pool, jedis);
        }
        return result;
    }
    //----------------------------------- set operation ------------------
    /**
     * Set operation : add
     * 
     * @param key : setname
     * @return
     */
    public static Long sadd(String key, String value) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.sadd(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return result;
    }
    
    /**
     * 判断set中是否有值
     * 
     * @param key
     * @return
     */
    public static Boolean sismember(String key, String member) {
        Boolean result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.sismember(key, member);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            // 返还到连接池
            returnResource(pool, jedis);
        }

        return result;
    }
    /**
     * hashmap operation : set
     * 
     * @param
     * @return
     */
//    public static String hmset(String mapName, Map<String, String> map) {
//        String result = null;
//        JedisPool pool = null;
//        Jedis jedis = null;
//        try {
//            pool = getPool();
//            jedis = pool.getResource();
//            result = jedis.hmset(mapName, map);
//        } catch (Exception e) {
//            e.printStackTrace();
//            if (null != jedis) {
//                pool.returnBrokenResource(jedis);
//                jedis = null;
//            }
//        } finally {
//            // 返还到连接池
//            returnResource(pool, jedis);
//        }
//        return result;
//    }
    //----------------------------------- map operation ------------------
    /**
     * hashmap operation : set
     * add a single key value pair to hashmap.
     * 
     * @param
     * @return
     */
    public static Long hset(String mapName, String key, String value) {
        Long result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.hset(mapName, key, value);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }
    /**
     * hashmap operation : get
     * get a single value using a key from hashmap.
     * 
     * @param
     * @return
     */
    public static String hget(String mapName, String key) {
        String result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.hget(mapName, key);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            // 返还到连接池
            returnResource(pool, jedis);
        }
        return result;
    }
    /**
     * whether hashmap contains a field
     * 
     * @param key
     * @return
     */
    public static Boolean hexists(String map, String field) {
        Boolean result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.hexists(map, field);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            returnResource(pool, jedis);
        }
        return result;
    }
    /**
     * show all values to a key
     * 
     * @param key
     * @return
     */
    public static Map<String, String> hgetall(String map) {
        Map<String, String> result = null;
        JedisPool pool = null;
        Jedis jedis = null;
        try {
            pool = getPool();
            jedis = pool.getResource();
            result = jedis.hgetAll(map);
        } catch (Exception e) {
            e.printStackTrace();
            if (null != jedis) {
                pool.returnBrokenResource(jedis);
                jedis = null;
            }
        } finally {
            returnResource(pool, jedis);
        }
        return result;
    }

}
