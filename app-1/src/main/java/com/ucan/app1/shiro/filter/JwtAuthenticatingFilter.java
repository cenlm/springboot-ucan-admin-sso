package com.ucan.app1.shiro.filter;

import java.io.IOException;
import java.util.Objects;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson2.JSON;
import com.ucan.app1.base.response.MsgEnum;
import com.ucan.app1.base.response.Response;
import com.ucan.app1.shiro.token.JwtToken;
import com.ucan.app1.shiro.util.JwtTokenUtil;
import com.ucan.app1.shiro.util.TokenCookieManager;

/**
 * JWT 认证
 * 
 * @Description:
 * @author liming.cen
 * @date 2024-07-05 14:59:41
 * 
 */
public class JwtAuthenticatingFilter extends BasicHttpAuthenticationFilter {

    private TokenCookieManager tokenCookieManager;
    /**
     * token cookie有效期
     */
    private int tokenCookieMaxAge;

    /**
     * Tips: shiro过滤器方法执行顺序 onPreHandle --> isAccessAllowed --> executeLogin --><br>
     * JwtRealm#doGetAuthenticationInfo --> onAccessDenied<br>
     * 为了适配前后端分离项目调用而导致的跨域问题，这里预先进行跨域请求的响应处理
     */
    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 设置允许跨域访问的域名，可以使用通配符 * 表示允许所有域名访问
        httpResponse.setHeader("Access-Control-Allow-Origin", "*");
        // 设置允许的请求方法，例如 "GET, POST, OPTIONS"
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        // 设置允许的请求头字段，例如 "Content-Type, Authorization"
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, newAccessToken");
        // 浏览器在进行跨域请求前，会先发送一个OPTIONS预请求来检测服务器是否支持跨域请求所需的（CORS）配置
        if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            // 返回状态码 200，然后直接返回，OPTIONS请求不用继续执行isAccessAllowed与onAccessDenied方法
            httpResponse.setStatus(HttpStatus.OK.value());
            return false;
        }
        // 进入 isAccessAllowed、onAccessDenied具体实现的执行
        return super.onPreHandle(request, response, mappedValue);
    }

    /**
     * 通过发送 jwt token到sso认证系统来决定是否放行
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        // 从客户端获取jwt token
//        String token = getAuthzHeader(request);
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String fromLogout=request.getParameter("fromLogout");
        Cookie[] cookies = req.getCookies();
        String token = "";
        if (!Objects.isNull(cookies) && cookies.length > 0) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("tokenCookie")) {
                    token = cookies[i].getValue();
                    break;
                }
            }
        }

        if (!Objects.isNull(token) && !token.equals("")) {
            /**
             * 认证操作交给 {@code JwtRealm}{@link doGetAuthenticationInfo(AuthenticationToken
             * jwtToken) } 完成
             */
            try {
                boolean isLoginUrl = pathsMatch(getLoginUrl(), request);
                
                if (isLoginUrl) {// 如果是请求的是登录页面且token不为空，则交由/pass处理请求，进行页面token的验证与添加
                    resp.sendRedirect("/pass?fromLogout="+fromLogout);
                    return false;
                }
                return executeLogin(request, response);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            try {
                resp.sendRedirect("/pass");
                return false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 如果token==null，不做token校验，交由下一个过滤器进行处理
        return true;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        return false;
    }

    /**
     * 验证access token，如果认证失败，则会继续认证refresh token并尝试返回新的access token.<br>
     * 认证操作交给 {@code JwtRealm}{@link doGetAuthenticationInfo(AuthenticationToken
     * jwtToken) } 完成。<br>
     * 如果认证失败，将会抛出AuthenticationException异常
     */
    @Override
    protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        AuthenticationToken jwtToken = createToken(request, response);
        try {
            // 完成token验证、当前subject的principal、认证信息的填充
            Subject subject = getSubject(request, response);
            subject.login(jwtToken);
            Session session = subject.getSession(false);
            String newAccessToken = (String) session.getAttribute("newAccessToken");
            // 旧的access token失效，且可以refresh token验证通过且可以生成新的access token，
            // 则要更新tokenCookie
            if (!Objects.isNull(newAccessToken) && !newAccessToken.equals("")) {
                tokenCookieManager.setTokenCookie(newAccessToken, tokenCookieMaxAge, req, resp);
            }
        } catch (Exception e) {

//            String htmlContent = "<script>window.location.href='" + req.getContextPath()+"/logout" + "';</script>";
//            response.getWriter().write(htmlContent);alert('"+e.getMessage()+"');

            // 直接把异常响应给客户端，因为此时间点并未到达业务控制器处理入口，无法使用全局异常处理器
//            response.setContentType("application/json;charset=utf-8");
//            response.getWriter().write(
//                    JSON.toJSONString(Response.fail(MsgEnum.TOKEN_VERIFICATION_FAILED.getCode(), e.getMessage())));
//          
            response.setContentType("text/html");
            String htmlContent = "<script>" + "setTimeout(function() {" + // 退出系统
                    "window.location.href =" + req.getContextPath() + "'/logout';" + "} , 100);" + "</script>";
            response.getWriter().write(htmlContent);
            return false;
        }
        return true;
    }

    /**
     * 创建JwtToken
     */
    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
//        String token = getAuthzHeader(request);
        HttpServletRequest req = (HttpServletRequest) request;

        Cookie[] cookies = req.getCookies();
        String token = "";
        if (cookies.length > 0) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("tokenCookie")) {
                    token = cookies[i].getValue();
                    break;
                }
            }
        }
        return new JwtToken(token);
    }

    public void setTokenCookieManager(TokenCookieManager tokenCookieManager) {
        this.tokenCookieManager = tokenCookieManager;
    }

    public void setTokenCookieMaxAge(int tokenCookieMaxAge) {
        this.tokenCookieMaxAge = tokenCookieMaxAge;
    }

}
