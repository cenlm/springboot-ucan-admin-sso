package com.ucan.sso.server.controller.sso;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ucan.sso.server.base.response.MsgEnum;
import com.ucan.sso.server.base.response.Response;
import com.ucan.sso.server.entity.User;
import com.ucan.sso.server.exception.CustomException;
import com.ucan.sso.server.service.IUserService;
import com.ucan.sso.server.shiro.util.EncryptionUtil;
import com.ucan.sso.server.shiro.util.JwtBase64Util;
import com.ucan.sso.server.shiro.util.LimitLoginRecorder;
import com.ucan.sso.server.shiro.util.TokenCookieManager;
import com.ucan.sso.server.util.DomainUtil;
import com.ucan.sso.server.util.sso.JwtUtil;

/**
 * @Description: sso单点登录服务控制器
 * @author liming.cen
 * @date 2024-07-16 20:49:45
 * 
 */
@Controller
public class SsoServerController {
    private static Logger log = LoggerFactory.getLogger(SsoServerController.class);
    @Autowired
    private IUserService userService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private TokenCookieManager tokenCookieManager;
    @Autowired
    private CacheManager redisCacheManager;
    @Autowired
    private LimitLoginRecorder limitLoginRecorder;

    @RequestMapping("/toLogin")
    public String toLogin() {
        return "login";

    }

    /**
     * 单点登录，JWT token生成
     * 
     * @param username
     * @param password
     * @return
     */
    @RequestMapping("/login")
    @ResponseBody
    public String login(@RequestParam(name = "username", required = true, defaultValue = "") String username,
            @RequestParam(name = "password", required = true, defaultValue = "") String password,
            @RequestParam(name = "target", required = false) String target, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        String msg = "";
        String rootDomain = "";
        int port = -1;
        String protocol = "";
        String successTo = "";
        if (username == "" || password == "") {
            return JSON.toJSONString(Response.fail("用户名|密码不能为空！"));
        }
        boolean status = limitLoginRecorder.getLimitStatus();
        if (status) {// 用户已被限制登录
            String rs = limitLoginRecorder.ban2Login(username);
            if (!rs.equals("")) {
                return rs;
            }
        }

        User user = userService.queryByName(username);
        if (user == null) {// 没有查询到用户
            return JSON.toJSONString(Response.fail("用户名或密码错误！"));
        } else if (user.getIsEnable() == 0) {
            return JSON.toJSONString(Response.fail("该用户已被禁用！"));
        }
        // 密码匹配，说明用户登录成功，生成JWT token
        if (user.getPassword().equals(EncryptionUtil.md5Encode(password))) {
            // 生成失败后抛异常
            Map<String, String> tokenFromLogin = jwtUtil.createTokenFromLogin(username, user.getUserId());

            User userObjUser = new User();
            userObjUser.setUserId(user.getUserId());
            // 设置登录时间
            userObjUser.setEntryDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            // 设置上次登录时间
            userObjUser.setLastLogin(user.getEntryDate());

            // 更新登录时间和上次登录时间
            userService.update(userObjUser);
            String accessToken = tokenFromLogin.get("accessToken");
            String refreshToken = tokenFromLogin.get("refreshToken");
            if (!Objects.isNull(target) && !target.equals("")) {// 从target字符串中获取协议、根域名、端口号，用于设置cookie的域
                protocol = DomainUtil.getProtocol(target);// 协议
                rootDomain = DomainUtil.getRootDomain(target);// 根域名
                port = DomainUtil.getPort(target);// 端口号
                // successTo: 设置目标token cookie的域
                if (port == -1) {// 端口号为 80，可以省略不写
                    successTo = protocol + "://" + rootDomain + "/addToken?accessToken=" + accessToken;
                } else {
                    successTo = protocol + "://" + rootDomain + ":" + port + "/addToken?accessToken=" + accessToken;
                }
            }
            // 这里的cookie在 /jump 中用到
            tokenCookieManager.setTokenCookie(accessToken, 86400, request, response);

            Cache<String, String> refreshTokenCache = redisCacheManager.getCache("refreshToken");
            // 保存refreshToken到缓存数据库
            refreshTokenCache.put(username, refreshToken);

            msg = JSON.toJSONString(Response.respCustomMsgWithData(MsgEnum.SUCCESS.getCode(), "登录成功！", successTo));
            //登录成功，解禁登录限制
            limitLoginRecorder.unban2Login(username);
        } else {
            String result = limitLoginRecorder.increaseFailLoginCounts(username);
            if (result.equals("")) {// 登录失败次数未达到指定阈值
                return JSON.toJSONString(Response.fail("用户名或密码错误！"));
            }
            return result;
        }
        return msg;
    }

    /**
     * 跨域验证与同步
     * 
     * @param username
     * @param password
     * @param fromLogout 是否是退出登录转发过来的请求: "1" 是 ，其他值 不是
     * @return
     */
    @RequestMapping("/jump")
//    @ResponseBody
    public String jump(@RequestParam("target") String target,
            @RequestParam(name = "fromLogout", required = false) String fromLogout, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
//拼接.ucan.com 域下的cookie到http://www.umall.com/add?xxxx中，然后对该地址发起请求，设置.umall.com域下的cookie
        Cookie[] cookies = request.getCookies();
        String token = "";
        String protocol = "";
        String rootDomain = "";
        int port = -1;
        if (!Objects.isNull(cookies) && cookies.length > 0) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("tokenCookie")) {
                    token = cookies[i].getValue();
                    break;
                }
            }
        }
        // 1.验证tokenCookie中的accessToken，如果验证成功，拼接accessToken至target目标url/addToken?accessToken=accessToken，然后进行重定向
        // 2.如果验证失败，则获取该用户的refreshToken，并尝试生成新的accessToken
        // 3.accessToken生成成功，更新.ucan.com域的tokenCookie，拼接accessToken至target目标url/addToken?accessToken=accessToken，然后进行重定向
        if (token.equals("")) {// 如果accessToken为空，则直接跳转到sso登录页面
            return "redirect:/toLogin?target=" + URLEncoder.encode(target, "UTF-8");
        } else {// 子系统进行logOut操作之后跳转的 /toLogin 会被拦截而转发到 /pass ，最后会转发到 SSO 的/jump
            if (!Objects.isNull(fromLogout) && fromLogout.equals("1")) {
                // 删除浏览器中SSO域下的tokenCookie，直接跳转到SSO登录页面，不用再进行token的校验
                tokenCookieManager.setTokenCookie("del", 0, request, response);
                return "redirect:/toLogin?target=" + URLEncoder.encode(target, "UTF-8");
            }
            boolean verifyJWT = false;
            if (!Objects.isNull(target) && !target.equals("")) {// 从target字符串中获取域名，用于设置cookie的域
                protocol = DomainUtil.getProtocol(target);
                rootDomain = DomainUtil.getRootDomain(target);
                port = DomainUtil.getPort(target);
            }
            try {
                // 验证accessToken
                verifyJWT = jwtUtil.verifyJWT(token);
            } catch (CustomException e) {
                log.error(e.getMessage());
                // 旧accessToken验证失败，尝试去获取对应用户的refreshToken，并尝试生成新的accessToken
                // 从旧accessToken中解析userName
                JSONObject payload = JwtBase64Util.getPayload(token);
                String userName = payload.getString("userName");
                // 获取对应用户的refreshToken
                Cache<String, String> refreshTokenCache = redisCacheManager.getCache("refreshToken");
                String refreshToken = refreshTokenCache.get(userName);
                String newAccessToken = "";
                try {
                    // 成功生成新的accessToken，更新sso系统的tokenCookie，拼接accessToken，重定向
                    newAccessToken = jwtUtil.updateAccessToken(refreshToken);
                    tokenCookieManager.setTokenCookie(newAccessToken, 86400, request, response);
                    String redirectUrl = protocol + "://" + rootDomain + (port != -1 ? (":" + port) : "")
                            + "/addToken?accessToken=" + newAccessToken + "&target=" + URLEncoder
                                    .encode(protocol + "://" + rootDomain + (port != -1 ? (":" + port) : ""), "UTF-8");
                    return "redirect:" + redirectUrl;
                } catch (CustomException e1) {
                    log.error(e1.getMessage());
                    return "redirect:/toLogin?target=" + URLEncoder.encode(target, "UTF-8");
                }
            }

            if (verifyJWT) {// 旧的accessToken验证成功，拼接accessToken，重定向
                String redirectUrl = protocol + "://" + rootDomain + (port != -1 ? (":" + port) : "")
                        + "/addToken?accessToken=" + token + "&target="
                        + URLEncoder.encode(protocol + "://" + rootDomain + (port != -1 ? (":" + port) : ""), "UTF-8");
                return "redirect:" + redirectUrl;
            } else {// 旧accessToken验证失败，尝试去获取对应用户的refreshToken，并尝试生成新的accessToken
                // 从旧accessToken中解析userName
                JSONObject payload = JwtBase64Util.getPayload(token);
                String userName = payload.getString("userName");
                // 获取对应用户的refreshToken
                Cache<String, String> refreshTokenCache = redisCacheManager.getCache("refreshToken");
                String refreshToken = refreshTokenCache.get(userName);
                String newAccessToken = "";
                try {// 成功生成新的accessToken，更新sso系统的tokenCookie，拼接accessToken，重定向
                    newAccessToken = jwtUtil.updateAccessToken(refreshToken);
                    tokenCookieManager.setTokenCookie(newAccessToken, 86400, request, response);
                    String redirectUrl = protocol + "://" + rootDomain + (port != -1 ? (":" + port) : "")
                            + "/addToken?accessToken=" + newAccessToken + "&target=" + URLEncoder
                                    .encode(protocol + "://" + rootDomain + (port != -1 ? (":" + port) : ""), "UTF-8");
                    return "redirect:" + redirectUrl;
                } catch (CustomException e) {
                    log.error(e.getMessage());
                    return "redirect:/toLogin?target=" + URLEncoder.encode(target, "UTF-8");
                }
            }
        }
    }

    /**
     * 生成新的access token返回给客户端
     * 
     * @param token
     * @return
     * @throws CustomException
     * @throws Exception
     */
    @RequestMapping("/updateToken")
    @ResponseBody
    public String updateAccessToken(
            @RequestParam(name = "refreshToken", required = true, defaultValue = "") String refreshToken)
            throws CustomException {
        String newAccessToken = "";
        try {
            newAccessToken = jwtUtil.updateAccessToken(refreshToken);
        } catch (CustomException e) {
            throw new CustomException(e.getMessage());
        }
        return JSON.toJSONString(
                Response.respCustomMsgWithData(MsgEnum.SUCCESS.getCode(), "已成功创建新的access token！", newAccessToken));
    }

    /**
     * 校验token
     * 
     * @param token
     * @return
     * @throws Exception
     */
    @RequestMapping("/verify")
    @ResponseBody
    public String verifyJWT(@RequestParam(name = "token", required = true, defaultValue = "") String token)
            throws Exception {
        jwtUtil.verifyJWT(token);
        return JSON.toJSONString(Response.success("token校验成功!"));
    }

    @RequestMapping("/logout")
    public String logOut() {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();
        return "redirect:/toLogin";
    }

}
