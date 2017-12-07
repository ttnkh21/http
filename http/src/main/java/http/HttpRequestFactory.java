package http;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.vise.log.Logger;

import java.io.File;
import java.util.Map;

import api.ApiException;
import api.HttpLib;
import api.ObtainPath;
import callback.ResultCallback;
import callback.UCallback;
import domain.wrap.CommonRequest;
import loading.ILoadingI;
import okhttp3.ResponseBody;

/**
 * 时间：2017/12/5 下午1:24
 */

public class HttpRequestFactory {
    private static final String TAG = "HttpRequestFactory";

    // Retrofit + Rxjava 简单封装,不需要知道返回值时,一般统计上报不需要
    public static <T> void exec(Object obj) {
        exec(obj, null, false);
    }

    public static <T> void doPost(Object obj, ResultCallback<T> mCallback) {
        exec(obj, mCallback, true);
    }

    public static <T> void doPost(Object obj, ResultCallback<T> mCallback, ILoadingI mILoadingI) {
        exec(obj, mCallback, true, mILoadingI);
    }

    public static <T> void doGet(Object obj, ResultCallback<T> mCallback) {
        exec(obj, mCallback, false);
    }

    public static <T> void exec(Object obj, ResultCallback<T> mCallback, boolean isPost) {
        exec(obj, mCallback, isPost, null);
    }

    public static <T> void exec(Object obj, ResultCallback<T> mCallback, boolean isPost, ILoadingI mILoadingI) {
        if (obj == null) {
            throw new RuntimeException("请求体不能为空~");
        }
        String pathPostfix = "";
        if (obj instanceof CommonRequest) {
            long start = SystemClock.currentThreadTimeMillis();
            CommonRequest common = (CommonRequest) obj;
            ObtainPath mObtainPath = common.getClass().getAnnotation(
                    ObtainPath.class);
            pathPostfix = mObtainPath.value();
            // 1220563
            if (!TextUtils.isEmpty(common.mAutoPrams)) {
                pathPostfix += common.mAutoPrams;
            }
            long end = SystemClock.currentThreadTimeMillis();
            Logger.e(TAG, "##解析耗时##" + (end - start) + "##毫秒##" + (end - start) / 1000
                    + "##秒##");
            if (TextUtils.isEmpty(pathPostfix)) {
                throw new RuntimeException("网络请求路径后缀不能为空!~");
            }
        } else {
            throw new RuntimeException("请求类型必须是CommonReq 或者 其子类吆~");
        }

        if (!Network.isConnected(HttpLib.getContext())) {
            // ToastTool.showNetisDead(HttpLib.getContext());
            // 没有网络
            if (mILoadingI != null && mILoadingI.isShowingI()) {
                mILoadingI.beginDismiss();
            }
            return;
        }
        if (isPost) {
            HttpRequestManager.doPost(pathPostfix, obj, mCallback == null ? new ResultCallback() {

                @Override
                public void onError(ApiException e) {
                    Logger.e(TAG, "#返回错误#" + e.getMessage());
                }

                @Override
                public void onResponse(Object response) {
                    Log.d(TAG, "#返回结果是#" + response);
                }
            } : mCallback, mILoadingI);
        } else {
            HttpRequestManager.doGet(pathPostfix, obj, mCallback == null ? new ResultCallback() {

                @Override
                public void onError(ApiException e) {
                    Logger.e(TAG, "#返回错误#" + e.getMessage());
                }

                @Override
                public void onResponse(Object response) {
                    Logger.e(TAG, "#返回结果是#" + response);
                }
            } : mCallback, mILoadingI);
        }
    }

    public static void downFile(String url, Map<String, String> maps, UCallback mUCallback, ResultCallback<ResponseBody> mCallback) {
        HttpRequestManager.downFile(url, maps, mUCallback, mCallback);
    }

    public static void uploadFiles(String url, Map<String, String> maps, UCallback mUCallback, ResultCallback<ResponseBody> mCallback, File... files) {
        HttpRequestManager.uploadFile(url, maps, mUCallback, mCallback, files);
    }
}
