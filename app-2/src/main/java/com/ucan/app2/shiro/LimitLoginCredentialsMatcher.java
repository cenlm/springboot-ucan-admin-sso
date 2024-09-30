package com.ucan.app2.shiro;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.DisabledAccountException;
import org.apache.shiro.authc.credential.SimpleCredentialsMatcher;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.ucan.app2.shiro.token.JwtToken;
import com.ucan.app2.shiro.util.JwtBase64Util;

/**
 * @Description: 登录失败次数限制
 * @author liming.cen
 * @date 2023-03-29 11:18:28
 * 
 */
@Component("limitLoginMatcher")
public class LimitLoginCredentialsMatcher extends SimpleCredentialsMatcher {

    @Autowired
    private CacheManager redisCacheManager;

    /**
     * 限制用户失败登录次数
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        boolean isCredentialsMatch = true;
        // 失败登录次数计数缓存
        Cache<String, AtomicInteger> attemptsCache = redisCacheManager.getCache("failLoginCount");
        // 限制登录时长计数缓存
        Cache<String, Date> limitTimer = redisCacheManager.getCache("limitTimer");
        JwtToken jwtToken = (JwtToken) token;
        String principal = (String) jwtToken.getPrincipal();
        JSONObject payload = JwtBase64Util.getPayload(principal);
        String userName = payload.getString("userName");
        String failLoginCountKey = "fail_login_attempts_" + userName;
        String limitTimerKey = "limit_login_timer_" + userName;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd SS:mm:ss");
        Date expireDate = new Date(payload.getInteger("exp").longValue());
        String msg = "";

        // 如果token已过期
        if (c.after(expireDate)) {

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
                msg = "对不起，您操作太频繁，请联系管理员重置密码！";
            }
            if (msg != "") {
                throw new DisabledAccountException(msg);
            }

        }
        Date timeAllowToLogin = limitTimer.get(limitTimerKey);
        AtomicInteger limitCount = attemptsCache.get(failLoginCountKey);
        if (!Objects.isNull(timeAllowToLogin) && !Objects.isNull(limitCount)) {
            long leftLimitedTime = System.currentTimeMillis() - timeAllowToLogin.getTime();
            if (leftLimitedTime < 0) {// 限制时长未结束，不允许登录操作
                throw new DisabledAccountException("连续" + limitCount.incrementAndGet() + "次登录失败，该账号已被限制登录，请联系管理员重置密码！");
            } else {
                // 已过了限制时长，清除之前用户的登录失败信息记录
                attemptsCache.remove(failLoginCountKey);
                limitTimer.remove(limitTimerKey);
            }
        }
        if (isCredentialsMatch) {
            // 登录成功，清除之前缓存的用户登录失败信息记录
            attemptsCache.remove(failLoginCountKey);
            limitTimer.remove(limitTimerKey);
        }
        return isCredentialsMatch;
    }

}
