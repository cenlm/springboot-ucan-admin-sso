package com.ucan.sso.server.shiro.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPooled;

/**
 * @Description: redis缓存封装
 * @author liming.cen
 * @date 2024-08-03 09:54:46
 * 
 */
public class RedisCache<K, V> implements Cache<K, V> {

    private String name;
    private JedisPooled jedis;

    public RedisCache(String name) {
        this.name = name;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(K key) throws CacheException {
        byte[] bytes = jedis.hget(name.toString().getBytes(), key.toString().getBytes());
        if (bytes != null) {
            try {
                // 反序列化
                ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
                ObjectInput in = new ObjectInputStream(bis);
                return (V) in.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    public V put(K key, V value) throws CacheException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            // 序列化value
            oos = new ObjectOutputStream(bos);
            oos.writeObject(value);
        } catch (IOException e) {
            e.printStackTrace();
        }

        byte[] bytes = bos.toByteArray();
        // 使用redis hash数据结构存储数据
        jedis.hset(name.getBytes(), key.toString().getBytes(), bytes);
        return value;
    }

    @Override
    public V remove(K key) throws CacheException {
        byte[] value = jedis.hget(name.toString().getBytes(), key.toString().getBytes());
        jedis.hdel(name.toString().getBytes(), key.toString().getBytes());
        return (V) value;
    }

    @Override
    public void clear() throws CacheException {
        jedis.flushDB();
    }

    @Override
    public Set<K> keys() {
        LinkedHashSet<K> keys = new LinkedHashSet<K>();
        Map<byte[], byte[]> hgetAll = jedis.hgetAll(name.toString().getBytes());
        hgetAll.entrySet().forEach(entry -> {
            // 反序列化
            ByteArrayInputStream bis = new ByteArrayInputStream(entry.getKey());
            ObjectInput in;
            try {
                in = new ObjectInputStream(bis);
                keys.add((K) in.readObject());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return keys;
    }

    @Override
    public int size() {
        Map<byte[], byte[]> hgetAll = jedis.hgetAll(name.toString().getBytes());
        return hgetAll.size();
    }

    @Override
    public Collection<V> values() {
        LinkedHashSet<V> values = new LinkedHashSet<V>();
        Map<byte[], byte[]> hgetAll = jedis.hgetAll(name.toString().getBytes());
        hgetAll.entrySet().forEach(entry -> {
            // 反序列化
            ByteArrayInputStream bis = new ByteArrayInputStream(entry.getValue());
            ObjectInput in;
            try {
                in = new ObjectInputStream(bis);
                values.add((V) in.readObject());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return values;
    }

    /**
     * 注入jedis连接
     * 
     * @param jedis
     */
    public void setJedis(JedisPooled jedis) {
        this.jedis = jedis;
    }

}
