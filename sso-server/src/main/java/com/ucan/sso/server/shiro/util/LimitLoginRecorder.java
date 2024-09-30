package com.ucan.sso.server.shiro.util;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.ucan.sso.server.base.response.Response;

/**
 * @Description: 用户登录失败次数纪录器（有SSO认证系统只做JWT令牌的生成、认证和更新，没用到shiro的login方法，<br>
 *               所以不会用到SimpleCredentialsMatcher#doCredentialsMatch，只能自行实现登录次数限制的逻辑）
 * @author liming.cen
 * @date 2024-09-29 20:48:59
 * 
 */
@Component
public class LimitLoginRecorder {
    @Autowired
    private CacheManager redisCacheManager;

    /**
     * 用户登录禁用状态：默认为 false
     */
    private AtomicBoolean isLimited = new AtomicBoolean();

    /**
     * 累加用户登录失败次数
     * 
     * @param currentUserName
     */
    public String increaseFailLoginCounts(String currentUserName) {
        String msg = "";
        // 失败登录次数计数缓存
        Cache<String, AtomicInteger> attemptsCache = redisCacheManager.getCache("failLoginCount");
        // 限制登录时长计数缓存
        Cache<String, Date> limitTimer = redisCacheManager.getCache("limitTimer");
        String failLoginCountKey = "fail_login_attempts_" + currentUserName;
        String limitTimerKey = "limit_login_timer_" + currentUserName;

        // 从缓存中获取某个用户已登录失败的次数
        AtomicInteger failLoginCounts = attemptsCache.get(failLoginCountKey);

        if (Objects.isNull(failLoginCounts)) {
            AtomicInteger initCount = new AtomicInteger(1);
            attemptsCache.put(failLoginCountKey, initCount);
            failLoginCounts = attemptsCache.get(failLoginCountKey);
        } else {
            failLoginCounts.incrementAndGet();
            attemptsCache.put(failLoginCountKey, failLoginCounts);
        }
        int failLogin = failLoginCounts.get();
//      System.out.println(failLogin);
        if (!Objects.isNull(failLoginCounts) && failLogin >= 5 && failLogin < 10) {// 连续5次登录失败后触发
            if (failLogin == 5) {
                limitTimer.put(limitTimerKey, new Date(System.currentTimeMillis() + 1000 * 60 * 15));
            }
            msg = "连续" + failLogin + "次登录失败，请15分钟后再试！";
        } else if (!Objects.isNull(failLoginCounts) && failLogin >= 10 && failLogin < 15) {// 连续10登录失败后触发
            if (failLogin == 10) {
                limitTimer.put(limitTimerKey, new Date(System.currentTimeMillis() + 1000 * 60 * 45));
            }
            msg = "连续" + failLogin + "次登录失败，请45分钟后再试！";
        } else if (!Objects.isNull(failLoginCounts) && failLogin >= 15) {
            isLimited.compareAndSet(false, true);
            msg = "连续" + failLogin + "次登录失败，再次尝试会被限制登录！";
        }
        if (msg.equals("")) {
            return "";
        }
        return JSON.toJSONString(Response.fail(msg));
    }

    /**
     * 阻断登录操作或者限制时长已过，解除登录限制
     * 
     * @param currentUserName
     * @return
     */
    public String ban2Login(String currentUserName) {
        String msg = "";
        // 失败登录次数计数缓存
        Cache<String, AtomicInteger> attemptsCache = redisCacheManager.getCache("failLoginCount");
        // 限制登录时长计数缓存
        Cache<String, Date> limitTimer = redisCacheManager.getCache("limitTimer");
        String failLoginCountKey = "fail_login_attempts_" + currentUserName;
        String limitTimerKey = "limit_login_timer_" + currentUserName;
        Date timeAllowToLogin = limitTimer.get(limitTimerKey);
        AtomicInteger limitCount = attemptsCache.get(failLoginCountKey);
        if (!Objects.isNull(timeAllowToLogin) && !Objects.isNull(limitCount)) {
            long leftLimitedTime = System.currentTimeMillis() - timeAllowToLogin.getTime();
            if (leftLimitedTime < 0) {// 限制时长未结束，不允许登录操作
                msg = "连续" + limitCount.incrementAndGet() + "次登录失败，该账号已被限制登录，请联系管理员！";
            } else {
                // 已过了限制时长，清除之前用户的登录失败信息记录
                attemptsCache.remove(failLoginCountKey);
                limitTimer.remove(limitTimerKey);
                isLimited.compareAndSet(true, false);
            }
        } else {// 管理员协助手动解禁
            isLimited.compareAndSet(true, false);
        }
        if (msg.equals("")) {
            return "";
        }
        return JSON.toJSONString(Response.fail(msg));
    }
    /**
     * 多次登录失败后，如果用户进行一次成功登录操作，那么会重置用户禁用状态
     * @param currentUserName
     * @return
     */
    public void unban2Login(String currentUserName) {
        // 失败登录次数计数缓存
        Cache<String, AtomicInteger> attemptsCache = redisCacheManager.getCache("failLoginCount");
        // 限制登录时长计数缓存
        Cache<String, Date> limitTimer = redisCacheManager.getCache("limitTimer");
        String failLoginCountKey = "fail_login_attempts_" + currentUserName;
        String limitTimerKey = "limit_login_timer_" + currentUserName;
        // 登录成功，清除之前缓存的用户登录失败信息记录
        attemptsCache.remove(failLoginCountKey);
        limitTimer.remove(limitTimerKey);
        isLimited.compareAndSet(true, false);
    }

    /**
     * 获取用户登录禁用状态
     * 
     * @return
     */
    public boolean getLimitStatus() {
        return isLimited.get();
    }
}
