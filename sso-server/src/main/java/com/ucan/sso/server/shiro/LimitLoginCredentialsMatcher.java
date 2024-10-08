package com.ucan.sso.server.shiro;

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

/**
 * @Description: 登录失败次数限制（由于sso系统中压根就没用到shiro的login方法，所有这里的realm、CredentialsMatcher没啥用）
 * @author liming.cen
 * @date 2023-03-29 11:18:28
 * 
 */
@Component("limitLoginMatcher")
public class LimitLoginCredentialsMatcher extends SimpleCredentialsMatcher {

    @Autowired
    private CacheManager cacheManager;

    /**
     * 限制用户失败登录次数
     */
    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        // 失败登录次数计数缓存
        Cache<String, AtomicInteger> attemptsCache = cacheManager.getCache("failLoginCount");
        // 限制登录时长计数缓存
        Cache<String, Date> limitTimer = cacheManager.getCache("limitTimer");
        String currentUserName = (String) token.getPrincipal();
        String failLoginCountKey = "fail_login_attempts_" + currentUserName;
        String limitTimerKey = "limit_login_timer_" + currentUserName;

        String tokenCredential = new String((char[]) token.getCredentials());
        String authInfoCredential = (String) info.getCredentials();
        String msg = "";

        // 密码不匹配，登录失败
        if (!authInfoCredential.equals(tokenCredential)) {

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
//	    System.out.println(failLogin);
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
        boolean isLoginSuccess = super.doCredentialsMatch(token, info);
        if (isLoginSuccess) {
            // 登录成功，清除之前缓存的用户登录失败信息记录
            attemptsCache.remove(failLoginCountKey);
            limitTimer.remove(limitTimerKey);
        }
        return isLoginSuccess;
    }

}
