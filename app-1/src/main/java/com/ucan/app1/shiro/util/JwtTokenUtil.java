package com.ucan.app1.shiro.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ucan.app1.base.response.MsgEnum;
import com.ucan.app1.base.response.Response;
import com.ucan.app1.exception.CustomException;
import com.ucan.app1.util.HttpUtil;

/**
 * @Description: JwtToken工具类，登录认证后生成/验证/刷新 access token，生成/保存/验证 refresh token
 * @author liming.cen
 * @date 2024-07-21 20:04:30
 * 
 */
@Component
public class JwtTokenUtil {
    /**
     * 缓存管理器，主要用于处理refresh token
     */
    @Autowired
    private CacheManager redisCacheManager;
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
     * refresh token验证成功，返回新的access token
     */
    @Value("${ucan.sso.server.updateToken}")
    private String ssoUpdateTokenUrl;

    /**
     * 登录认证成功后，返回access token，保存refresh token到缓存数据库
     * 
     * @param userName
     * @param password
     * @return
     * @throws CustomException 抛异常，让全局异常处理器捕获
     */
    public String generateToken4Login(String userName, String password) throws CustomException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", userName);
        params.put("password", password);
        // 发送用户、密码到SSO认证服务器进行认证。认证成功，返回token
        String accessToken = "";
        try {
            String result = HttpUtil.post(params, ssoLoginUrl);
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (!Objects.isNull(jsonObject)) {
                Integer code = (Integer) jsonObject.get("code");
                String refreshToken = "";
                if (code == 0) {
                    accessToken = jsonObject.getJSONObject("data").getString("accessToken");
                    refreshToken = jsonObject.getJSONObject("data").getString("refreshToken");
                    Cache<String, String> refreshTokenCache = getRefreshTokenCache();
                    // 保存refreshToken到缓存数据库
                    refreshTokenCache.put(userName, refreshToken);
                }
            }
        } catch (IOException e) {
            throw new CustomException(e.getMessage());
        }
        return accessToken;
    }

    /**
     * 验证refresh token并返回新的access token
     * 
     * @param refreshToken
     * @return
     * @throws CustomException
     */
    public String updateAccessToken(String accessToken) throws CustomException {
        // access token验证失败，去验证当前用户的refresh token以判断是否需要返回新的access token
        Cache<String, String> refreshTokenCache = getRefreshTokenCache();
        // jwt token由三部分组成：HEADER.PAYLOAD.SIGNATURE
        JSONObject payload = JwtBase64Util.getPayload(accessToken);
        // 从payload里面取出userName
        String userName = payload.getString("userName");
        // 从缓存中查找refreshToken
        String refreshToken = refreshTokenCache.get(userName);
        String newAccessToken = "";
        if (!Objects.isNull(refreshToken) && !refreshToken.equals("")) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("refreshToken", refreshToken);
            try {
                // sso系统验证refresh token并返回新的access token
                String result = HttpUtil.post(params, ssoUpdateTokenUrl);
                JSONObject jsonObject = JSONObject.parseObject(result);
                if (!Objects.isNull(jsonObject)) {
                    Integer code = (Integer) jsonObject.get("code");
                    if (code == 0) {
                        newAccessToken = jsonObject.getString("data");
                    } else {
                        // refreshToken认证失败，从缓存中删除该用户的refreshToken
                        refreshTokenCache.remove(userName);
                        throw new CustomException(jsonObject.getString("msg"));
                    }
                }
            } catch (Exception e) {
                refreshTokenCache.remove(userName);
                throw new CustomException(e.getMessage());
            }
        }
        // newAccessToken = ""，说明refresh token 为空，没有进行验证操作，不会返回newAccessToken
        return newAccessToken;
    }

    /**
     * 验证客户端access token，如果验证失败，则去验证该用户的refresh token<br>
     * refresh token 验证成功后会尝试返回新access token
     * 
     * @param token
     * @return
     */
    public String verifyAccessToken(String token) {
        // 否则发送token到sso服务器进行校验
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", token);
        String result = "";
        String newAccessToken = "";
        try {
            // 验证access token
            result = HttpUtil.post(params, ssoTokenVerifyUrl);
        } catch (Exception e) {// access token 验证异常，通过refresh token继续认证并更新access token
            if (e instanceof IOException) {
                return JSON.toJSONString(Response.fail("远程token验证服务调用失败！"));
            }
            try {
                newAccessToken = updateAccessToken(token);
                return JSON.toJSONString(Response.respCustomMsgWithData(MsgEnum.SUCCESS.getCode(),
                        "认证成功：旧token过期，已生成新的token！", newAccessToken));
            } catch (CustomException e1) {
                return JSON.toJSONString(Response.fail(MsgEnum.TOKEN_VERIFICATION_FAILED.getCode(), e1.getMessage()));
            }

        }

        JSONObject jsonObject = JSONObject.parseObject(result);
        Integer code = jsonObject.getInteger("code");
        if (code != 0) {// access token校验失败，通过refresh token继续认证并更新access token
            try {
                newAccessToken = updateAccessToken(token);
                if (!newAccessToken.equals("") && !Objects.isNull(newAccessToken)) {
                    return JSON.toJSONString(Response.respCustomMsgWithData(MsgEnum.SUCCESS.getCode(),
                            "认证成功：旧token过期，已生成新的token！", newAccessToken));
                } else {
                    return JSON.toJSONString(
                            Response.fail(MsgEnum.FAIL.getCode(), "旧token过期，但没有refresh token协助认证，请重新登录！"));
                }
            } catch (CustomException e1) {
                return JSON.toJSONString(Response.fail(MsgEnum.TOKEN_VERIFICATION_FAILED.getCode(), e1.getMessage()));
            }
        }
        return JSON.toJSONString(Response.success("access token验证成功！"));
    }

    /**
     * 获取refreshToken
     */
    public Cache<String, String> getRefreshTokenCache() {
        Cache<String, String> cache = redisCacheManager.getCache("refreshToken");
        return cache;
    }
}
