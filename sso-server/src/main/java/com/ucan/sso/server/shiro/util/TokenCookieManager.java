package com.ucan.sso.server.shiro.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.stereotype.Component;

import com.ucan.sso.server.util.DomainUtil;

/**
 * @Description: token cookie管理器
 * @author liming.cen
 * @date 2024-08-02 09:51:23
 * 
 */
@Component
public class TokenCookieManager {
    /**
     * 设置tokenCookie 的域为当前根域名
     * 
     * @param token
     * @param cookieMaxAge cookie有效期，单位：秒
     * @param request
     * @param response
     */
    public void setTokenCookie(String token, int cookieMaxAge, HttpServletRequest request,
            HttpServletResponse response) {
        if (token == null || token.equals("")) {
            String msg = "token 不能为空！";
            throw new IllegalArgumentException(msg);
        }
        String protocol = request.getScheme();
        String host = request.getServerName();

        String rootDomain = DomainUtil.getRootDomain(protocol + "://" + host);
        SimpleCookie cookie = new SimpleCookie("tokenCookie");
        // cookie有效期：1天，-1：浏览器关闭 立即清除
        cookie.setMaxAge(cookieMaxAge);
        cookie.setHttpOnly(true);
        cookie.setDomain("." + rootDomain);
        cookie.setValue(token);
//        cookie.setSecure(true);
//        cookie.setSameSite(SameSiteOptions.NONE);
        cookie.saveTo(request, response);

    }
}
