package com.ucan.app1.shiro.session;

import java.io.Serializable;

import org.apache.shiro.cache.AbstractCacheManager;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;

import com.ucan.app1.shiro.cache.RedisCache;

/**
 * @Description: 使用redis缓存session
 * @author liming.cen
 * @date 2024-08-03 11:19:01
 * 
 */
public class RedisCacheSessionDAO extends CachingSessionDAO {
    public RedisCacheSessionDAO() {
        setCacheManager(new AbstractCacheManager() {
            @Override
            protected Cache<Serializable, Session> createCache(String name) throws CacheException {
                return new RedisCache<Serializable, Session>(name);
            }
        });
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        return sessionId;
    }

    @Override
    protected void doUpdate(Session session) {
        // TODO Auto-generated method block

    }

    @Override
    protected void doDelete(Session session) {
        // TODO Auto-generated method block

    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        // TODO Auto-generated method block
        return null;
    }

}
