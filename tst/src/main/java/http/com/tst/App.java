package http.com.tst;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.vise.log.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import api.HttpLib;
import common.config.CommonConfig;
import convert.IGsonFactory2;
import interceptor.HttpLogInterceptor;
import interceptor.NoCacheInterceptor;
import okhttp3.Cache;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import util.SSLUtil;

/**
 * 作者：马俊
 * 时间：2017/12/5 下午3:43
 * 邮箱：747673016@qq.com
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        http();
    }


    private void http() {
        Logger.getLogConfig()
                .configAllowLog(CommonConfig.DEBUG)//是否输出日志,日志开关
                .configShowBorders(true)//是否排版显示
                .configTagPrefix("Logger")//设置标签前缀
                .configFormatTag("%d{HH:mm:ss:SSS} %t %c{-5}")//个性化设置标签，默认显示包名
                .configLevel(Log.VERBOSE);//设置日志最小输出级别，默认Log.VERBOSE

        HttpLib.config(this)//配置请求主机地址
                .baseUrl(CommonConfig.API_HOST="https://api.douban.com/v2/")
                //.baseUrl(CommonConfig.API_HOST)
                //配置全局请求头
                .globalHeaders(new HashMap<String, String>())
                //配置全局请求参数
                .globalParams(new HashMap<String, String>())
                //配置读取超时时间，单位秒
                .readTimeout(30)
                //配置写入超时时间，单位秒
                .writeTimeout(30)
                //配置连接超时时间，单位秒
                .connectTimeout(30)
                //配置请求失败重试次数
                .retryCount(3)
                //配置请求失败重试间隔时间，单位毫秒
                .retryDelayMillis(1000)
                //配置是否使用cookie
                //.setCookie(true)
                //配置自定义cookie
                //.apiCookie(new ApiCookie(this))
                //配置是否使用OkHttp的默认缓存
                .setHttpCache(true)
                //配置OkHttp缓存路径
                .setHttpCacheDirectory(new File(HttpLib.getContext().getCacheDir(), CommonConfig.CACHE_HTTP_DIR))
                //配置自定义OkHttp缓存
                .httpCache(new Cache(new File(HttpLib.getContext().getCacheDir(), CommonConfig.CACHE_HTTP_DIR), CommonConfig.CACHE_MAX_SIZE))
                //配置自定义离线缓存
                .cacheOffline(new Cache(new File(HttpLib.getContext().getCacheDir(), CommonConfig.CACHE_HTTP_DIR), CommonConfig.CACHE_MAX_SIZE))
                //配置自定义在线缓存
                .cacheOnline(new Cache(new File(HttpLib.getContext().getCacheDir(), CommonConfig.CACHE_HTTP_DIR), CommonConfig.CACHE_MAX_SIZE))
                //配置开启Gzip请求方式，需要服务器支持
//                .postGzipInterceptor()
                //配置应用级拦截器
                // .interceptor(new HttpLogInterceptor()
                //        .setLevel(HttpLogInterceptor.Level.BODY))
                // 打印全部日志
                .networkInterceptor(new HttpLogInterceptor()
                        .setLevel(HttpLogInterceptor.Level.BODY))
                //配置网络拦截器
                .networkInterceptor(new NoCacheInterceptor())
                //配置转换工厂
                .converterFactory(IGsonFactory2.create())
                //配置适配器工厂
                .callAdapterFactory(RxJava2CallAdapterFactory.create())
                //配置请求工厂
//                .callFactory(new Call.Factory() {
//                    @Override
//                    public Call newCall(Request request) {
//                        return null;
//                    }
//                })
                //配置连接池
                // .connectionPool(new ConnectionPool())
                //配置主机证书验证
                .hostnameVerifier(new SSLUtil.UnSafeHostnameVerifier("http://192.168.1.100/"))
                //配置SSL证书验证
                .SSLSocketFactory(SSLUtil.getSslSocketFactory(null, null, null)).build();
    }
}
