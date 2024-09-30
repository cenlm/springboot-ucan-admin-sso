package com.ucan.app1.shiro.realm;

import java.util.Objects;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;

import com.alibaba.fastjson2.JSONObject;
import com.ucan.app1.shiro.util.JwtBase64Util;

/**
 * @Description: Jwt token认证信息类
 * @author liming.cen
 * @date 2024-07-22 16:59:24
 * 
 */
public class JwtAuthenticationInfo extends SimpleAuthenticationInfo {

    private static final long serialVersionUID = 1L;

    public JwtAuthenticationInfo(Object principal, Object credentials, String realmName) {
        super(principal, credentials, realmName);
    }

    /**
     * 设置session范围的newAccessToken、currentUserId，方便SecurityUtils.getSubject()对象获取
     * 
     * @param newAccessToken
     */
    public void setNewAccessToken(String newAccessToken) {
        JSONObject payload = JwtBase64Util.getPayload(newAccessToken);
        String userId = payload.getString("userId");
        Session session = SecurityUtils.getSubject().getSession(false);
        if (!Objects.isNull(session)) {
            session.setAttribute("newAccessToken", newAccessToken);
            session.setAttribute("currentUserId", userId);
        }

    }
}
