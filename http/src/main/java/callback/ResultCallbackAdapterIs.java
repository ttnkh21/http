package callback;

import android.app.Activity;

import com.vise.log.Logger;

import api.ApiException;

/**
 * 时间：2017/12/5 下午4:22
 */

public abstract class ResultCallbackAdapterIs<T> extends ResultCallback<T> {
    // public RecyclerView mRecyclerView;
    public static final String TAG = ResultCallbackAdapterIs.class.getSimpleName();

    public ResultCallbackAdapterIs(Activity mContext) {
        this.mContext = mContext;
    }

    public Activity mContext;

//    public ResultCallbackAdapterIs(RecyclerView mRecyclerView) {
//        this.mRecyclerView = mRecyclerView;
//    }
//
//    public ResultCallbackAdapterIs(RecyclerView mRecyclerView, Activity mContext) {
//        this.mRecyclerView = mRecyclerView;
//        this.mContext = mContext;
//    }

    public ResultCallbackAdapterIs() {

    }

    @Override
    public void onError(ApiException ex) {
        doCommonTask();
        Logger.e(TAG, ex);
        if (mContext != null && mContext.isFinishing()) {
            Logger.e(TAG, "#activity已经死亡#");
            return;
        }
        doOnError(ex);
    }

    private void doCommonTask() {
        if (mContext != null && mContext.isFinishing()) {
            Logger.e(TAG, "#activity已经死亡#");
            return;
        }
//        if (mRecyclerView != null && mRecyclerView instanceof VerticalRecyclerView) {
//            VerticalRecyclerView mRecyclerView = (VerticalRecyclerView) this.mRecyclerView;
//            Logger.e(TAG, "#尝试刷新界面#");
//            mRecyclerView.doRefresh();
//            mRecyclerView.reset();
//        }
    }

    @Override
    public void onResponse(T response) {
        doCommonTask();
        if (mContext != null && mContext.isFinishing()) {
            Logger.e(TAG, "#activity已经死亡#");
            return;
        }
        doOnResponse(response);
        Logger.e(TAG, response);
    }

    /**
     * 成功回调,包裹
     *
     * @param response
     */
    public void doOnResponse(T response) {
        Logger.e(TAG, response);
    }

    /**
     * 失败回调,包裹
     *
     * @param ex
     */
    public void doOnError(ApiException ex) {
        Logger.e(TAG, "#doOnError#" + ex);
    }
}
