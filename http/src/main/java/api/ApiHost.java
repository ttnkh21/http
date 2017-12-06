package api;

import common.config.CommonConfig;

/**
 * 时间：2017/12/5 下午3:28
 */

public class ApiHost {
    private static String HOST = CommonConfig.API_HOST;

    public static String getHost() {
        return HOST;
    }

    public static void setHost(String url) {
        setHostHttps(url);
    }

    public static void setHostHttp(String url) {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            HOST = url;
            HOST = HOST.replaceAll("https://", "http://");
        } else {
            HOST = "http://" + url;
        }
    }

    public static void setHostHttps(String url) {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            HOST = url;
            HOST = HOST.replaceAll("http://", "https://");
        } else {
            HOST = "https://" + url;
        }
    }

    public static String getHttp() {
        if (HOST.startsWith("https://") || HOST.startsWith("http://")) {
            HOST = HOST.replaceAll("https://", "http://");
        } else {
            HOST = "http://" + HOST;
        }
        return HOST;
    }

    public static String getHttps() {
        if (HOST.startsWith("https://") || HOST.startsWith("http://")) {
            HOST = HOST.replaceAll("http://", "https://");
        } else {
            HOST = "https://" + HOST;
        }
        return HOST;
    }
}
