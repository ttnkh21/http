package common.respwrap;

import android.os.Handler;
import android.text.TextUtils;

import com.google.gson.JsonSyntaxException;
import com.vise.log.Logger;

import api.ApiException;
import callback.ResultCallback;
import common.config.CommonConfig;
import http.HttpRequestManager;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableObserver;
import loading.ILoadingI;

/**
 * 时间：2017/12/5 下午2:15
 */

public abstract class CommonRespWrapIs extends DisposableObserver<String> {
    public String currPathPostfix; // 请求后缀
    public Handler mHandler;
    public ILoadingI mLoading;
    public ResultCallback<?> mCallback;
    public static final String CODE = "code";
    public static final String MSG = "msg";
    public static final String DATA = "data";
    public static final int SUCCESS = 1;
    public String TAG;

    public CommonRespWrapIs(String currPathPostfix, Handler mHandler,
                            ResultCallback<?> mCallback, ILoadingI mloading) {
        this.currPathPostfix = currPathPostfix;
        this.mHandler = mHandler;
        this.mCallback = mCallback;
        this.mLoading = mloading;
        this.TAG = getClass().getSimpleName();
//        if (this.mloading != null) {
//            mloading.setOnDismissListener(new DialogInterface.OnDismissListener() {
//                @Override
//                public void onDismiss(DialogInterface dialogInterface) {
//                    Logger.e("task", "取消网络请求##");
//                    // HttpRequestManager.doCancelHttpTask();
//                }
//            });
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLoading != null) {
            mLoading.beginShow();
        }
        // Logger.e("KK", "=任务开始=" + currPathPostfix);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void execErrorCallback(final Throwable e,
                                  final ResultCallback<?> callback, final ILoadingI mloading) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    dismiss(mloading);
                    if (callback != null) {
                        ApiException apiException = null;
                        if (e != null) {
                            if (e instanceof ApiException) {
                                apiException = (ApiException) e;
                            } else {
                                apiException = new ApiException(e);
                            }
                        }
                        callback.onError(apiException);
                    }
                }
            });
        }
    }


    public void dismiss(ILoadingI mloading) {
        try {
            if (mloading != null) {
//                Context mCtx = mloading.getContext();
//                if (mCtx != null) {
//                    if (mCtx instanceof Activity && ((Activity) mCtx).isFinishing()) {
//                       //  Logger.e("KK", "失败mloading.beginDismiss()");
//                        return;
//                    }
//                }
                if (mloading.isShowingI()) {
                    mloading.beginDismiss();
                    // Logger.e("KK", "成功mloading.beginDismiss()");
                }
            }
        } catch (Throwable e) {
            // Logger.e("KK", "失败mloading.beginDismiss()" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Throwable error) {
        if (error != null) {
            printLog(error.getMessage()+"#errormsgs#");
        }
        execErrorCallback(error, mCallback, mLoading);
    }


    public void printLog(@NonNull String resp) {
        if (CommonConfig.DEBUG) {
            Logger.e(TAG, CommonConfig.API_HOST + currPathPostfix);
            try {
                String tag = currPathPostfix.split("/")[1];
                Logger.e(tag, resp);
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    String tag = currPathPostfix.split("/")[0];
                    Logger.e(tag, resp);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            System.out.println("|========|");
            System.out.println(CommonConfig.API_HOST + currPathPostfix);
            System.out.println(resp);
            System.out.println("|========|");
        }
    }

    @Override
    public void onComplete() {
        // Logger.e("KK", "=任务完成=" + currPathPostfix);
        dismiss(mLoading);
    }

    /**
     * 只需要在这里处理响应体即可,成功响应<br/>
     */
    @Override
    public void onNext(String resp) {
        printLog(resp);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void execSuccessCallback(final Object o, final ResultCallback callback, final ILoadingI mloading) {
        if (mHandler != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    dismiss(mLoading);
                    if (callback != null) {
                        callback.onResponse(o);
                    }
                }
            });
        }
    }

    /**
     * string对象处理<br/>
     */
    public void paserToString(String resp) {
        if (mCallback.mType == String.class) {
            if (TextUtils.isEmpty(resp)) {
                Exception e = new Exception("返回结果为空!!!");
                execErrorCallback(e, mCallback, mLoading);
            } else {
                execSuccessCallback(resp, mCallback, mLoading);
            }
        }
    }

    /**
     * 泛型对象处理<br/>
     */
    public void parseToObjs(String resp) {
        if (TextUtils.isEmpty(resp)) {
            Exception e = new Exception("返回结果为空!!!");
            execErrorCallback(e, mCallback, mLoading);
        } else {
            try {
                long start = System.currentTimeMillis();
                Object obj = HttpRequestManager.GSON.fromJson(resp, mCallback.mType);
                long end = System.currentTimeMillis();
                // Logger.log(TAG, "#类型是#" + mCallback.mType);
                Logger.e("RESP", "##解析耗时##" + (end - start) + "##毫秒##" + (end - start) / 1000
                        + "##秒##");
                if (obj == null) {
                    Exception e = new Exception(
                            "mGson.fromJson(finalStr,callback.mType) return null!");
                    execErrorCallback(e, mCallback, mLoading);
                } else {
                    execSuccessCallback(obj, mCallback, mLoading);
                }
            } catch (JsonSyntaxException e) {
                Exception ex = new Exception(
                        "|当前请求路径|" + currPathPostfix + "-||--->" + e);
                execErrorCallback(ex, mCallback, mLoading);
                e.printStackTrace();
            }
        }
    }

}
