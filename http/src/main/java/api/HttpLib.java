package api;

import android.content.Context;

import common.config.HttpGlobalConfig;
import http.HttpRequestManager;

/**
 * 时间：2017/12/5 下午3:31
 */

public class HttpLib {
    public static Context CONTEXT;
    private static final HttpGlobalConfig NET_GLOBAL_CONFIG = HttpGlobalConfig.getInstance();

    public static HttpGlobalConfig config() {
        return NET_GLOBAL_CONFIG;
    }

    public static HttpGlobalConfig config(Context mContext) {
        init(mContext);
        HttpRequestManager.init(mContext);
        return NET_GLOBAL_CONFIG;
    }

    public static void init(Context mContext) {
        if (CONTEXT == null) {
            CONTEXT = mContext;
        }
    }

    public static Context getContext() {
        if (CONTEXT == null) {
            throw new RuntimeException("请先初始化context");
        }
        return CONTEXT;
    }
}
