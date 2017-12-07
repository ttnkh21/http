package http;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.vise.log.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import api.Api;
import api.ApiException;
import api.ApiHost;
import api.HttpLib;
import cache.ApiCache;
import callback.ResultCallback;
import callback.UCallback;
import common.config.CommonConfig;
import common.config.HttpGlobalConfig;
import common.respwrap.CommonRespWrapI;
import convert.IGsonFactory2;
import headers.HttpHeaders;
import interceptor.DownLoadProgressInterceptor;
import interceptor.HeadersInterceptor;
import interceptor.UploadProgressRequestBody;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import loading.ILoadingI;
import mime.MediaTypes;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retry.RetryFunc;
import util.SSLUtil;

/**
 * 时间：2017/12/5 下午1:27
 * 网络请求管家<br/>
 */

public class HttpRequestManager {
    public static final String BASE_URL = "";
    public static final Gson GSON = new Gson();
    public static final String TAG = HttpRequestManager.class.getSimpleName();
    public OkHttpClient mOkHttpClient;
    public Retrofit mRetrofit;
    public int retryDelayMillis;//请求失败重试间隔时间
    public int retryCount;//重试次数
    public Handler mHandler;
    public String currPathPostfix; // 当前网络请求后缀
    public List<Interceptor> interceptors = new ArrayList<>();//局部请求的拦截器
    public List<Interceptor> networkInterceptors = new ArrayList<>();//局部请求的网络拦截器
    public HttpHeaders headers = new HttpHeaders();//请求头
    //protected String baseUrl;//基础域名
    public Object tag;//请求标签
    public long readTimeOut;//读取超时时间
    public long writeTimeOut;//写入超时时间
    public long connectTimeOut;//连接超时时间
    public boolean isHttpCache;//是否使用Http缓存
    protected String baseUrl;//基础域名
    private static OkHttpClient.Builder mOkHttpBuilder;
    private static Retrofit.Builder mRetrofitBuilder;
    private static ApiCache.Builder mApiCacheBuilder;
    private static ApiCache mApiCache;
    private Context mContext;
    HttpGlobalConfig mHttpGlobalConfig;
    protected UCallback mUploadCallback;//上传进度回调

    //=========单例=========
    private HttpRequestManager() {
        mHandler = new Handler(Looper.getMainLooper());
        init();
    }

    /**
     * 初始化时机不对,再次调用一次修正问题<br/>
     */
    public void init() {
        mHttpGlobalConfig = HttpLib.config();
        mContext = HttpLib.getContext();
        generateGlobalConfig();
        generateLocalConfig();
        mOkHttpClient = mOkHttpBuilder.build();
        mRetrofit = mRetrofitBuilder.build();
    }

    public static void init(Context mContext) {

        if (mOkHttpBuilder == null) {
            mOkHttpBuilder = new OkHttpClient.Builder();
        }
        if (mRetrofitBuilder == null) {
            mRetrofitBuilder = new Retrofit.Builder();
        }
        if (mApiCacheBuilder == null) {
            mApiCacheBuilder = new ApiCache.Builder(mContext);
        }
    }

    /**
     * 生成局部配置
     */
    protected void generateLocalConfig() {
        OkHttpClient.Builder newBuilder = getOkHttpBuilder().build().newBuilder();

        if (mHttpGlobalConfig.getGlobalHeaders() != null) {
            headers.put(mHttpGlobalConfig.getGlobalHeaders());
        }

        if (!interceptors.isEmpty()) {
            for (Interceptor interceptor : interceptors) {
                newBuilder.addInterceptor(interceptor);
            }
        }

        if (!networkInterceptors.isEmpty()) {
            for (Interceptor interceptor : networkInterceptors) {
                newBuilder.addNetworkInterceptor(interceptor);
            }
        }

        if (headers.headersMap.size() > 0) {
            newBuilder.addInterceptor(new HeadersInterceptor(headers.headersMap));
        }

        if (mUploadCallback != null) {
            // newBuilder.addNetworkInterceptor(new UploadProgressInterceptor(mUploadCallback));
            newBuilder.addNetworkInterceptor(new DownLoadProgressInterceptor(mUploadCallback));
        }

        if (readTimeOut > 0) {
            newBuilder.readTimeout(readTimeOut, TimeUnit.SECONDS);
        }

        if (writeTimeOut > 0) {
            newBuilder.readTimeout(writeTimeOut, TimeUnit.SECONDS);
        }

        if (connectTimeOut > 0) {
            newBuilder.readTimeout(connectTimeOut, TimeUnit.SECONDS);
        }

        if (isHttpCache) {
            try {
                if (mHttpGlobalConfig.getHttpCache() == null) {
                    mHttpGlobalConfig.httpCache(new Cache(mHttpGlobalConfig.getHttpCacheDirectory(), CommonConfig.CACHE_MAX_SIZE));
                }
                mHttpGlobalConfig.cacheOnline(mHttpGlobalConfig.getHttpCache());
                mHttpGlobalConfig.cacheOffline(mHttpGlobalConfig.getHttpCache());
            } catch (Exception e) {
                // ViseLog.e("Could not create http cache" + e);
            }
            newBuilder.cache(mHttpGlobalConfig.getHttpCache());
        }

        if (baseUrl != null) {
            Retrofit.Builder newRetrofitBuilder = new Retrofit.Builder();
            newRetrofitBuilder.baseUrl(baseUrl);
            if (mHttpGlobalConfig.getConverterFactory() != null) {
                newRetrofitBuilder.addConverterFactory(mHttpGlobalConfig.getConverterFactory());
            }
            if (mHttpGlobalConfig.getCallAdapterFactory() != null) {
                newRetrofitBuilder.addCallAdapterFactory(mHttpGlobalConfig.getCallAdapterFactory());
            }
            if (mHttpGlobalConfig.getCallFactory() != null) {
                newRetrofitBuilder.callFactory(mHttpGlobalConfig.getCallFactory());
            }
            newBuilder.hostnameVerifier(new SSLUtil.UnSafeHostnameVerifier(baseUrl));
            newRetrofitBuilder.client(newBuilder.build());
            mRetrofit = newRetrofitBuilder.build();
        } else {
            getRetrofitBuilder().client(newBuilder.build());
            mRetrofit = getRetrofitBuilder().build();
        }
    }

    /**
     * 生成全局配置
     */
    protected void generateGlobalConfig() {
        if (mHttpGlobalConfig.getBaseUrl() == null) {
            mHttpGlobalConfig.baseUrl(ApiHost.getHost());
        }
        getRetrofitBuilder().baseUrl(mHttpGlobalConfig.getBaseUrl());
        Logger.e("URL=" + mHttpGlobalConfig.getBaseUrl());
        if (mHttpGlobalConfig.getConverterFactory() != null) {
            getRetrofitBuilder().addConverterFactory(mHttpGlobalConfig.getConverterFactory());
        } else {
            getRetrofitBuilder().addConverterFactory(IGsonFactory2.create());
        }

        if (mHttpGlobalConfig.getCallAdapterFactory() == null) {
            mHttpGlobalConfig.callAdapterFactory(RxJava2CallAdapterFactory.create());
        }
        getRetrofitBuilder().addCallAdapterFactory(mHttpGlobalConfig.getCallAdapterFactory());

        if (mHttpGlobalConfig.getCallFactory() != null) {
            getRetrofitBuilder().callFactory(mHttpGlobalConfig.getCallFactory());
        }

        if (mHttpGlobalConfig.getHostnameVerifier() == null) {
            mHttpGlobalConfig.hostnameVerifier(new SSLUtil.UnSafeHostnameVerifier(mHttpGlobalConfig.getBaseUrl()));
        }
        getOkHttpBuilder().hostnameVerifier(mHttpGlobalConfig.getHostnameVerifier());

        if (mHttpGlobalConfig.getSslSocketFactory() == null) {
            mHttpGlobalConfig.SSLSocketFactory(SSLUtil.getSslSocketFactory(null, null, null));
        }
        getOkHttpBuilder().sslSocketFactory(mHttpGlobalConfig.getSslSocketFactory());

        if (mHttpGlobalConfig.getConnectionPool() == null) {
            mHttpGlobalConfig.connectionPool(new ConnectionPool(CommonConfig.DEFAULT_MAX_IDLE_CONNECTIONS,
                    CommonConfig.DEFAULT_KEEP_ALIVE_DURATION, TimeUnit.SECONDS));
        }
        getOkHttpBuilder().connectionPool(mHttpGlobalConfig.getConnectionPool());

        if (mHttpGlobalConfig.isCookie() && mHttpGlobalConfig.getApiCookie() == null) {
            // mHttpGlobalConfig.apiCookie(new ApiCookie(getContext()));
        }
        if (mHttpGlobalConfig.isCookie()) {
            getOkHttpBuilder().cookieJar(mHttpGlobalConfig.getApiCookie());
        }

        if (mHttpGlobalConfig.getHttpCacheDirectory() == null) {
            mHttpGlobalConfig.setHttpCacheDirectory(new File(mContext.getCacheDir(), CommonConfig.CACHE_HTTP_DIR));
        }
        if (mHttpGlobalConfig.isHttpCache()) {
            try {
                if (mHttpGlobalConfig.getHttpCache() == null) {
                    mHttpGlobalConfig.httpCache(new Cache(mHttpGlobalConfig.getHttpCacheDirectory(),
                            CommonConfig.CACHE_MAX_SIZE));
                }
                mHttpGlobalConfig.cacheOnline(mHttpGlobalConfig.getHttpCache());
                mHttpGlobalConfig.cacheOffline(mHttpGlobalConfig.getHttpCache());
            } catch (Exception e) {
                // ViseLog.e("Could not create http cache" + e);
            }
        }
        if (mHttpGlobalConfig.getHttpCache() != null) { // 加缓存
            getOkHttpBuilder().cache(mHttpGlobalConfig.getHttpCache());
        }
    }

    public static Retrofit.Builder getRetrofitBuilder() {
        return getInstance().mRetrofitBuilder;
    }

    public static ApiCache.Builder getApiCacheBuilder() {
        return getInstance().mApiCacheBuilder;
    }

    public static OkHttpClient.Builder getOkHttpBuilder() {
        return getInstance().mOkHttpBuilder;
    }

    public static ApiCache getApiCache() {
        if (mApiCache == null || mApiCache.isClosed()) {
            mApiCache = getApiCacheBuilder().build();
        }
        return mApiCache;
    }

    private static final class HttpManagerHolder {
        private static final HttpRequestManager INSTANCE = new HttpRequestManager();
    }

    public static HttpRequestManager getInstance() {
        return HttpManagerHolder.INSTANCE;
    }
    //=========单例=========

    public static String reqParams(Object src) {
        String json = "";
        if (src != null && src instanceof String) {
            json = (String) src;

        } else {
            if (src == null) {
            } else {
                json = GSON.toJson(src);
            }
        }
        return json;
    }

    public static void doPost(String pathPostfix, Object src, final ResultCallback<?> mCallback, ILoadingI mloading) {
        getInstance().runHttpTask(pathPostfix, src, mCallback, mloading, true);
    }

    public static void doGet(String pathPostfix, Object src, final ResultCallback<?> mCallback, ILoadingI mloading) {
        getInstance().runHttpTask(pathPostfix, src, mCallback, mloading, false);
    }

    public static void doPost(String pathPostfix, Object src, final ResultCallback<?> mCallback) {
        doPost(pathPostfix, src, mCallback, null);
    }

    public static void doGet(String pathPostfix, Object src, final ResultCallback<?> mCallback) {
        doGet(pathPostfix, src, mCallback, null);
    }

    /**
     * 请求入口
     *
     * @param pathPostfix
     * @param src
     * @param mCallback
     * @param mloading
     * @param <T>
     */
    public <T> void runHttpTask(String pathPostfix, Object src, final ResultCallback<T> mCallback, ILoadingI mloading, boolean isPost) {
        String json = reqParams(src);
        Logger.e(TAG, "#请求后缀名#" + pathPostfix + "\n#请求参数#" + json);
        Api api = generateApi();
        currPathPostfix = pathPostfix;
        if (isPost) {
            exec(api.runPost(pathPostfix, json), mCallback, mloading);
        } else {
            exec(api.runGet(pathPostfix, json), mCallback, mloading);
        }
    }

    public <T> void exec(Observable<String> obj, final ResultCallback<T> mCallback, final ILoadingI mloading) {
        obj.compose(mTransformer)
                .subscribe(new CommonRespWrapI(currPathPostfix, mHandler, mCallback, mloading));
    }

    /**
     * 变身<br/>
     */
    ObservableTransformer<String, String> mTransformer = new ObservableTransformer<String, String>() {

        @Override
        @NonNull
        public ObservableSource<String> apply(@NonNull Observable<String> apply) {
            return apply.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).retryWhen(new RetryFunc(retryCount, retryDelayMillis));
        }

    };

    public Api generateApi() {
        Api api = mRetrofit.create(Api.class);
        return api;
    }

    /**
     * 尝试取消所有请求任务
     */
    public static void doCancelHttpTask() {
        OkHttpClient mOkHttpClient = getInstance().mOkHttpClient;
        if (mOkHttpClient != null) {
            Logger.e(TAG, "#取消网络请求任务#");
            try {
                mOkHttpClient.dispatcher().cancelAll();
            } catch (Exception e) {
                Logger.e(TAG, "#取消网络请求任务#" + e);
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据tag,取消请求
     *
     * @param tag
     */
    public void doCancelHttpTask(Object tag) {
        try {
            Dispatcher dispatcher = getInstance().mOkHttpClient.dispatcher();
            synchronized (dispatcher) {
                for (okhttp3.Call call : dispatcher.queuedCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
                for (okhttp3.Call call : dispatcher.runningCalls()) {
                    if (tag.equals(call.request().tag())) {
                        call.cancel();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void downFile(String url, Map<String, String> maps, UCallback mUCallback, ResultCallback<ResponseBody> mCallback) {
        getInstance().downOrUploadFileTask(url, maps, mUCallback, mCallback, true);
    }

    public static void uploadFile(String url, Map<String, String> maps, UCallback mUCallback, ResultCallback<ResponseBody> mCallback, File... files) {
        getInstance().downOrUploadFileTask(url, maps, mUCallback, mCallback, false, files);
    }

    protected List<MultipartBody.Part> multipartBodyParts = new ArrayList<>();

    public void downOrUploadFileTask(String url, Map<String, String> maps, UCallback mUCallback, final ResultCallback<ResponseBody> mCallback, boolean isDownLoad, File... files) {
        mUploadCallback = mUCallback;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (isDownLoad) {
            if (mUploadCallback != null) {
                // newBuilder.addNetworkInterceptor(new UploadProgressInterceptor(mUploadCallback));
                builder.addNetworkInterceptor(new DownLoadProgressInterceptor(mUploadCallback));
                // builder.addInterceptor(new DownLoadProgressInterceptor(mUploadCallback));
            }
        }
        OkHttpClient mHttpClient = builder.retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS).build();
        // generateLocalConfig();
        Api api = new Retrofit.Builder()
                .baseUrl(ApiHost.getHost())
                .client(mHttpClient)
                .addConverterFactory(IGsonFactory2.create())
                //配置适配器工厂
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(Api.class);
        Observable<ResponseBody> obs = null;
        if (isDownLoad) {
            obs = api.downFile(url, maps);
        } else {
            if (files != null && files.length != 0) {
                int index = 0;
                for (File file : files) {
                    addFile("key" + index, file, mUCallback);
                    index += 1;
                }
            }
            obs = api.uploadFiles(url, multipartBodyParts);
        }
        obs.compose(mTransformerII)
                .subscribe(new Observer<ResponseBody>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        System.out.println("执行onSubscribe");
                    }

                    @Override
                    public void onNext(ResponseBody responseBody) {
                        System.out.println("执行成功" + responseBody);
                        if (mCallback != null) {
                            mCallback.onResponse(responseBody);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        System.out.println("执行出错" + e);
                        if (mCallback != null) {
                            mCallback.onError(new ApiException(e));
                        }
                    }

                    @Override
                    public void onComplete() {
                        System.out.println("执行完毕");
                    }
                });
    }

    /**
     * 变身<br/>
     */
    ObservableTransformer<ResponseBody, ResponseBody> mTransformerII = new ObservableTransformer<ResponseBody, ResponseBody>() {

        @Override
        @NonNull
        public ObservableSource<ResponseBody> apply(@NonNull Observable<ResponseBody> apply) {
            return apply.subscribeOn(Schedulers.io())
                    .unsubscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

    };

    public HttpRequestManager addFile(String key, File file, UCallback callback) {
        if (key == null || file == null) {
            return this;
        }
        RequestBody requestBody = RequestBody.create(MediaTypes.APPLICATION_OCTET_STREAM_TYPE, file);
        if (callback != null) {
            UploadProgressRequestBody uploadProgressRequestBody = new UploadProgressRequestBody(requestBody, callback);
            MultipartBody.Part part = MultipartBody.Part.createFormData(key, file.getName(), uploadProgressRequestBody);
            this.multipartBodyParts.add(part);
        } else {
            MultipartBody.Part part = MultipartBody.Part.createFormData(key, file.getName(), requestBody);
            this.multipartBodyParts.add(part);
        }
        return this;
    }
}
