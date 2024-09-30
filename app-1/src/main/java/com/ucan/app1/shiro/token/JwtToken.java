package com.ucan.app1.shiro.token;

import org.apache.shiro.authc.AuthenticationToken;

/**
 * @Description: Jwt token 令牌
 * @author liming.cen
 * @date 2024-07-18 19:49:15
 * 
 */
public class JwtToken implements AuthenticationToken {
    private static final long serialVersionUID = 1368669440743802572L;
    private String principal;
    private String credentials;

    public JwtToken(String token) {
        this.principal = token;
        this.credentials = token;
    }

    /**
     * 获取principal，这里为客户端jwt token
     */
    @Override
    public Object getPrincipal() {
        return principal;
    }

    /**
     * 获取credentials，这里为客户端jwt token
     */
    @Override
    public Object getCredentials() {
        return credentials;
    }
}
