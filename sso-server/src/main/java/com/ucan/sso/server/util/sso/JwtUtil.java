package com.ucan.sso.server.util.sso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson2.JSONObject;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.IncorrectClaimException;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.MissingClaimException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.ucan.sso.server.exception.CustomException;
import com.ucan.sso.server.shiro.util.JwtBase64Util;

/**
 * @Description: Jwt工具类
 * @author liming.cen
 * @date 2024-07-16 20:51:52
 * 
 */
@Component
public class JwtUtil {
    private static Logger log = LoggerFactory.getLogger(JwtUtil.class);
    /**
     * 密钥
     */
    @Value("${ucan.sso.secretKey}")
    public String secretKey;
    /**
     * 签发者
     */
    @Value("${ucan.sso.issuer}")
    public String issuer;

    /**
     * expiresUnit 过期时间单位（0：秒，1：分，2：时，3：天 ）
     */
    @Value("${ucan.sso.expires-unit}")
    public int expiresUnit;

    /**
     * ExpiresAt token过期日期，必须为大于0的整数<br>
     * expires-unit=0 时表示expiresAt秒后过期，expires-unit=1时表示expiresAt分钟后过期，以此类推
     */
    @Value("${ucan.sso.expires-at}")
    public int expiresAt;

    /**
     * refreshToken相对于accessToken的过期时间增量，必须为大于0的整数。<br>
     * 时间单位共用expires-unit， expires-unit=0，expires-at=3，expires-at-incr=5时，<br>
     * 表示refreshToken 8秒后过期，以此类推，此数值必须大于0。
     */
    @Value("${ucan.sso.expires-at-incr}")
    public int expiresAtIncr;
    /**
     * 接收对象
     */
    @Value("${ucan.sso.audience}")
    public String audience;

    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 用户登录成功后，创建accessToken、refreshToken<br>
     * token的存储由客户端去处理
     * 
     * @param name
     * @param userId
     * @return
     * @throws CustomException
     */
    public Map<String, String> createTokenFromLogin(String name, String userId) throws CustomException {
        Map<String, String> tokenMap = new ConcurrentHashMap<String, String>();
        // accessToken
        String accessToken = createJWT(name, userId, false);
        // refreshToken
        String refreshToken = createJWT(name, userId, true);
        tokenMap.put("accessToken", accessToken);
        tokenMap.put("refreshToken", refreshToken);
        return tokenMap;
    }

    /**
     * access token过期后，去验证客户关联的refreshToken，refreshToken一般保存在客户端关系型数据库或其他存储介质。<br>
     * 如果验证成功则生成新的access token返回给客户端，<br>
     * 如果验证失败，说明两个token都过期了，要求用户重新登录认证。
     * 
     * @param refreshToken
     * @return
     * @throws CustomException
     */
    public String updateAccessToken(String refreshToken) throws CustomException {
        String newAccessToken = "";

        try {
            verifyJWT(refreshToken);
            JSONObject payload = JwtBase64Util.getPayload(refreshToken);
            String userName = payload.getString("userName");
            String userId = payload.getString("userId");
            newAccessToken = createJWT(userName, userId, false);
        } catch (CustomException e) {
            throw new CustomException("Refresh Token异常：" + e.getMessage() + "请重新登录认证！");
        }

        return newAccessToken;
    }

    /**
     * 创建JWT Token（accessToken或refreshToken）<br>
     * "sub" (Subject)：主题，用于标识JWT的主体，通常是用户ID或唯一标识符<br>
     * "iss" (Issuer)：签发者，用于标识JWT的签发者<br>
     * "exp" (Expiration Time)：过期时间，用于指定JWT的有效期限<br>
     * "nbf" (Not Before)：生效时间，在这个时间之前JWT不能被接受和处理<br>
     * "iat" (Issued At)：签发时间，用于标识JWT的签发时间<br>
     * "aud" (Audience)：受众，用于指定该JWT的预期接收者<br>
     * 
     * @param principal 用户名
     * @param userId    用户ID
     * @param isRefresh 判断是否在创建RefreshToken，RefreshToken的有效期必须比accessToken的长
     * @return
     * @throws CustomException
     */
    public String createJWT(String principal, String userId, boolean isRefresh) throws CustomException {
        String token = "";
        Calendar c = Calendar.getInstance();
        // 签发日期（当前日期）
        Date issuedDate = c.getTime();
        // 在此日期之前，该JWT token不能被接收和处理
        Date nbfDate = issuedDate;
        if (expiresUnit < 0 || expiresUnit > 3) {
            throw new CustomException("expiresUnit必须是 0~3 的整数");
        }
        if (expiresAt <= 0) {
            throw new CustomException("expiresAt必须是大于0的整数");
        }
        if (expiresAtIncr <= 0) {
            throw new CustomException("expiresAtIncr必须是大于0的整数");
        }
        int expires = 0;
        if (isRefresh) {// refreshToken过期时间
            expires = expiresAt + expiresAtIncr;
        } else { // accessToken过期时间
            expires = expiresAt;
        }

        switch (expiresUnit) {
        case 0:
            c.add(Calendar.SECOND, expires);
            break;
        case 1:
            c.add(Calendar.MINUTE, expires);
            break;
        case 2:
            c.add(Calendar.HOUR_OF_DAY, expires);
            break;
        default:
            c.add(Calendar.DAY_OF_YEAR, expires);
            break;
        }

        Date expiredDate = c.getTime();
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            token = JWT.create().withIssuer(issuer).withSubject(principal).withJWTId(userId).withIssuedAt(issuedDate)
                    .withNotBefore(nbfDate).withExpiresAt(expiredDate).withAudience(audience)
                    .withClaim("userId", userId).withClaim("userName", principal).sign(algorithm);
        } catch (JWTCreationException e) {
            // 签名配置无效，不能转换Claims
            throw new CustomException("token创建失败！", e.getCause());
        }
        log.info("SSO server创建token：" + token);
        return token;
    }

    /**
     * 验证JWT
     * 
     * @throws CustomException
     */
    public boolean verifyJWT(String token) throws CustomException {
        if (Objects.isNull(token) || token.equals("")) {// 用户还未没有token，需要登录生成
            throw new CustomException("token令牌不能为空！");
        }
        DecodedJWT decodedJWT;
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            JWTVerifier verifier = JWT.require(algorithm).withIssuer(issuer).build();
            decodedJWT = verifier.verify(token);
            // 验证通过，开始获取claims
            Map<String, Claim> claims = decodedJWT.getClaims();
            // 签发日期
            Date iatDate = claims.get("iat").asDate();
            // 过期时间
            Date expiredDate = claims.get("exp").asDate();
            // 生效日期
            Date nbfDate = claims.get("nbf").asDate();

            log.info("token=>" + "主体:" + claims.get("sub") + "，签发人：" + claims.get("iss") + "，签发日期："
                    + format.format(iatDate) + "，生效日期：" + format.format(nbfDate) + "，过期日期：" + format.format(expiredDate)
                    + "，userName：" + claims.get("userName") + "，userId：" + claims.get("userId") + "，接收对象："
                    + claims.get("aud"));
        } catch (JWTVerificationException e) {
            if (e instanceof TokenExpiredException) {
                throw new CustomException("token 已过期！");
            } else if (e instanceof SignatureVerificationException) {
                throw new CustomException("token 签名验证失败！");
            } else if (e instanceof JWTDecodeException) {
                throw new CustomException("token 解码失败，令牌无效或内容格式错误！");
            } else if (e instanceof MissingClaimException) {
                throw new CustomException("token 声明内容缺失！");
            } else if (e instanceof IncorrectClaimException) {
                throw new CustomException("token 声明内容不匹配！");
            } else if (e instanceof AlgorithmMismatchException) {
                throw new CustomException("抛出的异常与Header中声明的不匹配！");
            }
        }
        return true;
    }

//    public static void main(String[] args) {
//        JwtUtil jUtil = new JwtUtil();
//        try {
//            jUtil.createJWT("张三", "123456");
//            jUtil.verifyJWT("");
//        } catch (CustomException e) {
//            e.printStackTrace();
//        }
//    }

}
