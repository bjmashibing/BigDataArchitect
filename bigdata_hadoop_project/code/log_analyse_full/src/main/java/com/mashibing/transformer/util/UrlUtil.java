package com.mashibing.transformer.util;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * url工具类
 * 
 * @author 马士兵教育
 *
 */
public class UrlUtil {
    /**
     * 判断指定的host是否是一个有效的外链host，如果不是，那么直接返回false，如果是，返回true。
     * 
     * @param host
     * @return
     */
    public static boolean isValidateInboundHost(String host) {
        if ("www.sxt.com".equals(host) || "www.isxt.com".equals(host)) {
            return false;
        }
        return true;
    }

    /**
     * 获取指定url字符串中的host
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static String getHost(String url) throws MalformedURLException {
        URL u = getURL(url);
        return u.getHost();
    }

    /**
     * 根据字符串url创建一个URL对象
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */
    public static URL getURL(String url) throws MalformedURLException {
        url = url.trim();
        if (!(url.startsWith("http:") || url.startsWith("https:"))) {
            url = "http://" + url;
        }
        return new URL(url);
    }
}
