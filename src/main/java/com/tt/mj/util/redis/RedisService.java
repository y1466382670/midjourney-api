package com.tt.mj.util.redis;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * redis服务
 */
@Service
public class RedisService {


    public static String REDIS_PREFIX = "TTAPI_";

	public static final Integer EXPIRE = 3600;// 单位秒 2小时
    @Autowired(required = false)
    JedisPool jedisPool;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public String get(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //对key增加前缀，即可用于分类，也避免key重复
            String str = jedis.get(REDIS_PREFIX + key);
            return str;
        } finally {
            returnToPool(jedis);
        }

    }
    /**
     * 从redis连接池获取redis实例
     */
    public <T> T get(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //对key增加前缀，即可用于分类，也避免key重复
            String str = jedis.get(REDIS_PREFIX + key);
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }

    }

    public String set(String key, String value, int expire) {
        Jedis jedis = jedisPool.getResource();
        try {
            jedis.set(REDIS_PREFIX + key, value);
            if (expire != 0) {
                jedis.expire(REDIS_PREFIX + key, expire);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return value;
    }

    
    /**
     * 存储对象
     */
    public <T> Boolean set(String key, T value,int expireSeconds) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            if (expireSeconds <= 0) {
                jedis.set(REDIS_PREFIX +key, str);
            } else {
                jedis.setex(REDIS_PREFIX + key, expireSeconds, str);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }

    }

    /**
     * 删除
     */
    public boolean delete(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            long ret = jedis.del(REDIS_PREFIX + key);
            return ret > 0;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断key是否存在
     */
    public <T> boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(REDIS_PREFIX + key);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     * Redis Incr 命令将 key 中储存的数字值增一。    如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作
     */
    public <T> Long incrBy(String key, Integer step) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.incrBy(REDIS_PREFIX + key, step);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     */
    public <T> Long decrBy(String key, Integer step) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.decrBy(REDIS_PREFIX + key, step);
        } finally {
            returnToPool(jedis);
        }
    }


    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return String.valueOf(value);
        } else if (clazz == long.class || clazz == Long.class) {
            return String.valueOf(value);
        } else if (clazz == String.class) {
            return (String) value;
        } else {
            return JSON.toJSONString(value);
        }

    }

    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }
    
    public static <T> List<T> stringToList(String str, Class<T> clazz) {
    	
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        List<T> ts = (List<T>) JSONArray.parseArray(str, clazz);
        return ts;
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();//不是关闭，只是返回连接池
        }
    }

    
    public String setExpire(String key, int expire) {
        Jedis jedis = jedisPool.getResource();
        try {
            if (expire != 0) {
                jedis.expire(REDIS_PREFIX + key, expire);
            }
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return key;
    }
    
    public long getExpire(String key) {
        Jedis jedis = jedisPool.getResource();
        try {
        	return jedis.ttl(REDIS_PREFIX + key);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }

    /**
     * 获取List缓存.
     *
     * @param key 键
     * @return 值
     */
    public  List<String> getList(String key) {
        List<String> value = null;
        Jedis jedis = jedisPool.getResource();
        try {
            if (jedis.exists(key)) {
                value = jedis.lrange(REDIS_PREFIX + key, 0, -1);
            }
        }  finally {
            returnToPool(jedis);
        }
        return value;
    }

    /**
     * 设置List缓存.
     *
     * @param key          键
     * @param value        值
     * @param cacheSeconds 超时时间，0为不超时
     * @return
     */
    public  long setList(String key, List<String> value, int cacheSeconds) {
        long result = 0;
        Jedis jedis = jedisPool.getResource();
        try {
            if (jedis.exists(REDIS_PREFIX +key)) {
                jedis.del(REDIS_PREFIX + key);
            }
            result = jedis.rpush(key, value.toArray(new String[value.size()]));
            if (cacheSeconds != 0) {
                jedis.expire(REDIS_PREFIX + key, cacheSeconds);
            }
        }finally {
            returnToPool(jedis);
        }
        return result;
    }
    /**
     * 向List缓存中添加值.
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public long listAdd(String key, String... value) {
        long result = 0;
        Jedis jedis = jedisPool.getResource();
        try {
            result = jedis.rpush(REDIS_PREFIX + key, value);
        }  finally {
            returnToPool(jedis);
        }
        return result;
    }
// =============================common============================
    /**
     * 指定缓存失效时间
     *
     * @param key
     *            键
     * @param time
     *            时间(秒)
     * @return
     */
    public boolean expire(String key, long time) {
        try {
            if (time > 0) {
                redisTemplate.expire(REDIS_PREFIX + key, time, TimeUnit.SECONDS);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存获取
     *
     * @param key
     *            键
     * @return 值
     */
    public Object gets(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(REDIS_PREFIX + key);
    }
}
