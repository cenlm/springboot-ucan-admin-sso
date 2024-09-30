package com.ucan.app1.shiro.listener;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.DefaultSubjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.ucan.app1.shiro.util.JwtBase64Util;
import com.ucan.app1.shiro.util.JwtTokenUtil;

/**
 * @Description: 用户登录、退出监听器
 * @author liming.cen
 * @date 2024-07-09 21:21:10
 * 
 */
@Component("authenticationListener")
public class ShiroAuthenticationListener implements AuthenticationListener {
    private static Logger log = LoggerFactory.getLogger(ShiroAuthenticationListener.class);
    @Autowired
    @Qualifier("sessionDAO")
    private CachingSessionDAO shiroSessionDao;
    @Autowired
    private JwtTokenUtil tokenUtil;

    private Session session;

    Map<String, Session> activeSessionsMap = new ConcurrentHashMap<String, Session>();

    @Override
    public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
        // 认证成功后，获取已经分配给当前subject的session
        Subject subject = SecurityUtils.getSubject();
//        boolean isAuthenticated = subject.isAuthenticated();
//        boolean isRemembered = subject.isRemembered();
        session = subject.getSession(false);
        JSONObject payload = JwtBase64Util.getPayload(String.valueOf(token.getPrincipal()));
        String userName = payload.getString("userName");
        // 到onSuccess时间点的时候，用户已经认证成功，但 DelegatingSubject#login 方法还未执行到
        // this.authenticated = true
        if (!Objects.isNull(session)) {
            activeSessionsMap.put(userName, session);
            log.info("【" + userName + "】认证成功！sessionId:" + session.getId());
        } else {
            log.info("【" + userName + "】认证成功！");
        }
    }

    @Override
    public void onFailure(AuthenticationToken token, AuthenticationException ae) {
        JSONObject payload = JwtBase64Util.getPayload(String.valueOf(token.getPrincipal()));
        log.info("【" + payload.getString("userName") + "】认证失败！");
    }

    /**
     * 系统默认的退出行为只会删掉浏览器的rememberMe Cookie和移除掉session中纪录的principal和认证状态，<br>
     * 不会删除session，需要在onLogout回调方法中定义删除session的逻辑
     */
    @Override
    public void onLogout(PrincipalCollection principals) {
        String principal = (String) principals.getPrimaryPrincipal();
        JSONObject payload = JwtBase64Util.getPayload(principal);
        String userName = payload.getString("userName");
        Session session = activeSessionsMap.get(userName);
        if (!Objects.isNull(session)) {
            // 用户退出，清除当前已认证用户的session
            shiroSessionDao.delete(session);
        }

        log.info("【" + userName + "】已退出系统！");
        Cache<String, String> refreshTokenCache = tokenUtil.getRefreshTokenCache();
        if (!Objects.isNull(refreshTokenCache)) {
            // 退出系统时，把该用户的refreshTokenCache也清空
            refreshTokenCache.remove(userName);
        }
    }

}
