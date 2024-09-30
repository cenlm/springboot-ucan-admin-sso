package com.ucan;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;

public class UcanAdminApplicationTests {
    public static String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ1Y2FuLWFkbWluLXNzby1zZXJ2ZXIiLCJzdWIiOiLlvKDkuIkiLCJqdGkiOiIxMjM0NTYiLCJpYXQiOjE3MjEyMDAzMDgsIm5iZiI6MTcyMTIwMDMwOCwiZXhwIjoxNzIxNDU5NTA4LCJhdWQiOiJhcHAiLCJ1c2VySWQiOiIxMjM0NTYiLCJ1c2VyTmFtZSI6IuW8oOS4iSJ9.tnW3ljyQ19qKL9tIRS7Gq9WR8dD1Q__LkiMiS0SHj58";

    public static void main(String[] args) {
        UcanAdminApplicationTests test = new UcanAdminApplicationTests();
        try {
            test.testPOST();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 通过httpclient发送POST方式的请求
    public void testPOST() throws IOException {
        JSONObject jsonObject = new JSONObject();
        // 创建httpclient对象
        CloseableHttpClient httpClient = HttpClients.createDefault();

        // 创建请求对象
//        HttpPost httpPost = new HttpPost("http://localhost/ucan-sso/sso/verify");
//
//        jsonObject.put("token", token);
        HttpPost httpPost = new HttpPost("http://localhost/ucan-sso/sso/login");

        jsonObject.put("username", "admin");
        jsonObject.put("password", "123456");

        StringEntity entity = new StringEntity(jsonObject.toString());
        // 指定编码方式
        entity.setContentEncoding("utf-8");
        // 数据格式
        entity.setContentType("application/json");
//        post请求设置请求参数（JSON格式）
        httpPost.setEntity(entity);
        // 发送请求
        CloseableHttpResponse response = httpClient.execute(httpPost);
        // 解析返回结果
        // 获取服务端返回回来的状态码
        int statusCode = response.getStatusLine().getStatusCode();

        System.out.println("服务端返回的状态码: " + statusCode);

        // 获取服务端返回回来的响应体，然后通过一个工具类来解析这个响应体
        HttpEntity entity1 = response.getEntity();
        String body = EntityUtils.toString(entity1);

        System.out.println("服务端返回的数据是: " + body);

        // 关闭资源
        response.close();
        httpClient.close();
    }
}
