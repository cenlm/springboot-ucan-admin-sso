package com.ucan.app2.util;

import java.io.IOException;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @Description: http 请求工具
 * @author liming.cen
 * @date 2024-07-17 18:33:49
 * 
 */
public class HttpUtil {

    /**
     * 发送Post请求
     * 
     * @param params     请求参数，以Map形式封装
     * @param requestUrl 请求地址
     * @return
     * @throws IOException
     */
    public static String post(Map<String, String> params, String requestUrl) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Builder builder = new FormBody.Builder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            builder = builder.add(key, val);
        }

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder().url(requestUrl).post(requestBody).build();

        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    /**
     * 发送Get请求
     * 
     * @param requestUrl 请求地址
     * @return
     * @throws IOException
     */
    public static String get(String requestUrl) throws IOException {
        String responseBody;
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(requestUrl).build();

        try (Response response = client.newCall(request).execute()) {
            responseBody = response.body().string();
        }
        return responseBody;
    }
}
