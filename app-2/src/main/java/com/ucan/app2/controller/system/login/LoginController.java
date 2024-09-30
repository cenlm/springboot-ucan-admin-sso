package com.ucan.app2.controller.system.login;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ucan.app2.base.response.MsgEnum;
import com.ucan.app2.base.response.Response;
import com.ucan.app2.shiro.util.JwtTokenUtil;
import com.ucan.app2.shiro.util.TokenCookieManager;

/**
 * 用户登录控制器
 * 
 * @author liming.cen
 * @date 2022年12月27日 下午3:38:52
 */
@Controller
public class LoginController {
    @Autowired
    private JwtTokenUtil tokenUtil;

    @Autowired
    private TokenCookieManager tokenCookieManager;

    /**
     * sso认证系统的登录页面地址
     */
    @Value("${ucan.sso.server.toLogin}")
    private String sso2LoginUrl;

    /**
     * 远程SSO登录认证与token生成 接口地址
     */
    @Value("${ucan.sso.server.login}")
    private String ssoLoginUrl;

    /**
     * 远程SSO token校验 接口地址
     */
    @Value("${ucan.sso.server.verify}")
    private String ssoTokenVerifyUrl;

    /**
     * 系统默认的退出行为只会删掉浏览器的rememberMe Cookie和移除掉session中纪录的principal和认证状态，<br>
     * 不会删除session，需要在{@ShiroAuthenticationListener}{@link #onLogout(PrincipalCollection principals)}方法中定义session删除逻辑<br>
     * 以及从redis中删除refresh token，让当前用户的所有token都失效
     */
    @RequestMapping("/logout")
    public String logOut(RedirectAttributes redirectAttributes, HttpServletRequest request,
            HttpServletResponse response) {
        // 将数据添加到 RedirectAttributes 对象中
        redirectAttributes.addFlashAttribute("isLogout", "yes");
        Subject currentUser = SecurityUtils.getSubject();
      //删除浏览器的tokenCookie
        tokenCookieManager.setTokenCookie("del", 0, request, response);
        currentUser.logout();
        return "redirect:/toLogin?fromLogout=1";
    }

    @RequestMapping("/addToken")
    public String addToken(@RequestParam("accessToken") String accessToken,
            @RequestParam(name = "target", required = false) String target, HttpServletRequest request,
            HttpServletResponse response) {
        // 设置cookie， 有效期：1天，-1：浏览器关闭 立即清除
        tokenCookieManager.setTokenCookie(accessToken, 86400, request, response);
        // 重定向到/index，尝试登录认证
        return "redirect:/index";

    }

    @RequestMapping("/pass")
    public String toPassPage() {
        return "pass";
    }
//  已交由JwtAuthenticatingFilter 进行处理
//    @RequestMapping("/toLogin")
//    public String toLogin(HttpServletRequest request, HttpServletResponse response) {
//        // 先跳转到pass.ftl，重定向到login.ucan.com/jump?...，
//        // 从request中获取.ucan.com的cookie，如果有则进行验证，如果没有,则（携带target）直接跳转到登录页面；
//        // 验证、登录成功，拼接 .ucan.com 的cookie ，发送请求到 www.umall.com ，设置umall的cookie，在umall域下
//        // 携带cookie进行登录验证
//        String protocol = request.getScheme();
//        String domain = request.getServerName();
//        int port = request.getServerPort();
//        String url = "";
//        if (port == -1) {
//            url = sso2LoginUrl + "?target=" + protocol + "://" + domain;
//        } else {
//            url = sso2LoginUrl + "?target=" + protocol + "://" + domain + ":" + port;
//        }
//        return "redirect:" + url;
//
//    }

    /**
     * 客户端jwt token 验证
     * 
     * @param token
     * @return
     */
    @ResponseBody
    @RequestMapping("/verify")
    public String verifyToken(@RequestParam(name = "token", required = true) String token) {
        String result = tokenUtil.verifyAccessToken(token);
        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer code = jsonObject.getInteger("code");
        if (code == 0) {// token验证成功（包括通过refresh token协助的认证）
            String newAccessToken = jsonObject.getString("data");
            if (!Objects.isNull(newAccessToken) && !newAccessToken.equals("")) {
                return JSON.toJSONString(Response.respCustomMsgWithData(MsgEnum.SUCCESS.getCode(),
                        "认证成功，返回新的access token!", newAccessToken));
            }
            return JSON.toJSONString(Response.success("token验证成功！"));
        }
        return JSON
                .toJSONString(Response.fail(MsgEnum.TOKEN_VERIFICATION_FAILED.getCode(), jsonObject.getString("msg")));
    }

    /**
     * 已废弃，登录操作已交由 sso 系统处理
     * 
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
//    @Deprecated
//    public String login(@RequestParam(name = "username", required = true, defaultValue = "") String username,
//            @RequestParam(name = "password", required = true, defaultValue = "") String password) throws Exception {
//        String msg = "";
//        if (username == "" || password == "") {
//            return JSON.toJSONString(Response.fail("用户名|密码不能为空！"));
//        }
//        Subject currentUser = SecurityUtils.getSubject();
//        if (!currentUser.isAuthenticated() && !currentUser.isRemembered()) {/// IncorrectCredentialsException
//
//            User user = userService.queryByName(username);
//            if (user == null) {// 没有查询到用户
//                return JSON.toJSONString(Response.fail("用户名或密码错误！"));
//            } else if (user.getIsEnable() == 0) {
//                return JSON.toJSONString(Response.fail("该用户已被禁用！"));
//            }
//            // 从缓存中获取还存活的session
//            Collection<Session> activeSessions = sessionDAO.getActiveSessions();
//            if (activeSessions.size() > 0) {
//                activeSessions.forEach(s -> {
//                    SimplePrincipalCollection principal = (SimplePrincipalCollection) s
//                            .getAttribute(DefaultSubjectContext.PRINCIPALS_SESSION_KEY);
//                    if (!Objects.isNull(principal)) {
//                        String otherUser = (String) principal.getPrimaryPrincipal();
//                        // 将异地登录的账号踢出系统
//                        if (otherUser.equals(username)
//                                && user.getPassword().equals(EncryptionUtil.md5Encode(password))) {
//                            // 设置session立即超时
//                            s.setTimeout(0);
//                            sessionVerification.validateSpecifiedSession(s);
//                            SocketServer socketServer = socketServerManager.getSocketServer();
//                            WebSocket webSocket = socketServer.getSocket(otherUser + "_index");
//                            /**
//                             * 说明之前已登录系统的用户A的浏览器已关闭，已断开socket连接，<br>
//                             * 此时仅需要从服务器中删除用户A的session即可
//                             */
//                            if (Objects.isNull(webSocket)) {
//                                sessionDAO.delete(s);
//                            } else {// 之前已登录系统的用户A的浏览器未关闭，执行用户踢出流程
//                                webSocket.send("kickout");
//                                WebSocket socketFromLoginPage = socketServer.getSocket(username + "_login");
//                                socketFromLoginPage.send("正在踢出异地登录账号，登录操作6秒后进行");
//                                try {
//                                    Thread.sleep(6000);
//                                } catch (InterruptedException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        }
//                    }
//
//                });
//            }
//            // 登录认证成功后，返回access token，保存refresh token到缓存数据库
//            String accessToken = tokenUtil.generateToken4Login(username, password);
//            if (!Objects.isNull(accessToken) && !accessToken.equals("")) {
//                msg = JSON
//                        .toJSONString(Response.respCustomMsgWithData(MsgEnum.SUCCESS.getCode(), "登录成功！", accessToken));
//            } else {
//                msg = JSON.toJSONString(Response.fail("登录失败：用户名或密码错误，令牌获取失败！"));
//            }
//        } else {
//            msg = JSON.toJSONString(Response.success("用户已登录！"));
//        }
//        return msg;
//    }
}
