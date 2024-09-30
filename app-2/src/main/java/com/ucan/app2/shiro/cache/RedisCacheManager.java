package com.ucan.app2.shiro.cache;

import org.apache.shiro.ShiroException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Destroyable;
import org.apache.shiro.util.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPooled;

/**
 * @Description: redis 缓存管理器
 * @author liming.cen
 * @date 2024-08-03 09:47:44
 * 
 */
public class RedisCacheManager implements CacheManager, Initializable, Destroyable {
    private static Logger log = LoggerFactory.getLogger(RedisCacheManager.class);

    private JedisPooled jedis;

    @Override
    public <K, V> Cache<K, V> getCache(String name) throws CacheException {
        RedisCache<K, V> redisCache = new RedisCache<K, V>(name);
        redisCache.setJedis(jedis);
        return redisCache;
    }

    @Override
    public void destroy() throws Exception {
        log.info("RedisCacheManager销毁");

    }

    @Override
    public void init() throws ShiroException {
        log.info("RedisCacheManager初始化");

    }

    public JedisPooled getJedis() {
        return jedis;
    }

    /**
     * 在ShiroConfig中进行注入，连接池配置在 JedisConfig中进行
     * 
     * @param jedis
     */
    public void setJedis(JedisPooled jedis) {
        this.jedis = jedis;
    }

}
