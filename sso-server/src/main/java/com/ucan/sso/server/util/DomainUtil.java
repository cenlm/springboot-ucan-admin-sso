package com.ucan.sso.server.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @Description: 域名工具类
 * @author liming.cen
 * @date 2024-08-22 11:29:29
 * 
 */
public class DomainUtil {
    /**
     * 获取根域名
     * 
     * @param urlStr
     * @return
     */
    public static String getRootDomain(String urlStr) {
//        String urlStr = "http://umall.com?cross=1?yes=1724294914644";
        URL url;
        String rootDomain = "";
        try {
            url = new URL(urlStr);
            String host = url.getHost();
            // 点号
            int dotIndex = host.indexOf(".");
            int nextDotIndex = host.indexOf(".", dotIndex + 1);
            // 冒号
            int colonIndex = host.indexOf(':');
            if (colonIndex == -1) {
                rootDomain = nextDotIndex != -1 ? host.substring(dotIndex + 1) : host;
            } else {
                rootDomain = nextDotIndex != -1 ? host.substring(dotIndex + 1, colonIndex)
                        : host.substring(0, colonIndex);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return rootDomain;
    }

    /**
     * 获取端口号
     * 
     * @param urlStr
     * @return
     */
    public static int getPort(String urlStr) {
        int port = -1;
        try {
            URL url = new URL(urlStr);
            port = url.getPort();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return port;
    }

    public static void main(String[] args) {
        String urlStr = "http://umall.com?cross=1?yes=1724294914644";
        int port = getPort(urlStr);
        System.out.println(port);
    }
    /**
     * 获取协议
     * 
     * @param urlStr
     * @return
     */
    public static String getProtocol(String urlStr) {
        String protocol = "";
        URL url;
        try {
            url = new URL(urlStr);
            protocol = url.getProtocol();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return protocol;
    }
}
