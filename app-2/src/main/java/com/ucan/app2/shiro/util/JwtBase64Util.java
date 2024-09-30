package com.ucan.app2.shiro.util;

import java.util.Base64;

import com.alibaba.fastjson2.JSONObject;

/**
 * @Description: Jwt Base64工具
 * @author liming.cen
 * @date 2024-07-18 20:09:59
 * 
 */
public class JwtBase64Util {

    /**
     * 获取jwt token的header
     * 
     * @param token
     * @return
     */
    public static JSONObject getHeader(String token) {
        // jwt token由三部分组成：HEADER.PAYLOAD.SIGNATURE
        String[] tokenParts = token.split("\\.");
        return decode(tokenParts[0]);
    }

    /**
     * 获取jwt token的payload
     * 
     * @param token
     * @return
     */
    public static JSONObject getPayload(String token) {
        // jwt token由三部分组成：HEADER.PAYLOAD.SIGNATURE
        String[] tokenParts = token.split("\\.");
        return decode(tokenParts[1]);
    }

    /**
     * 解码
     * 
     * @param encodeStr
     * @return
     */
    public static JSONObject decode(String encodeStr) {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        byte[] decode = decoder.decode(encodeStr);
        JSONObject jsonObj = JSONObject.parseObject(new String(decode));
        return jsonObj;
    }

//    public static void main(String[] args) {
//        Base64Util.decode("eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ");
//    }
}
