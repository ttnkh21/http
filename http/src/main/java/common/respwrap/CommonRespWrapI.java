package common.respwrap;

import android.os.Handler;
import android.text.TextUtils;

import com.vise.log.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import api.ApiException;
import callback.ResultCallback;
import loading.ILoadingI;

/**
 * 时间：2017/12/5 下午2:01
 * 所有的网络返回结果经过这里,这里是网络请求结果的通用入口<br/>
 */

public class CommonRespWrapI extends CommonRespWrapIs {
    // 很多东西扔给父类了,....
    public CommonRespWrapI(String currPathPostfix, Handler mHandler, ResultCallback<?> mCallback, ILoadingI mloading) {
        super(currPathPostfix, mHandler, mCallback, mloading);
    }

    /**
     * 只需要在这里处理响应体即可,成功响应<br/>
     * 解密|at here<br/>
     */
    @Override
    public void onNext(String resp) {
        super.onNext(resp);
        doParseTask(resp);
    }

    private void doParseTask(String resp) {
        if (mCallback != null) {
            if (mCallback.mType != null) {
                System.out.println("#类型#" + mCallback.mType);
                if (mCallback.mType == String.class) {
                    paserToString(resp);
                } else {
                    parseToObjs(resp);
                }
            } else {
                throw new RuntimeException("不可达错误");
            }
        } else {
            // throw new RuntimeException("回调函数不能为空");
        }
    }

    /**
     * 服务器无法保证格式统一时,可以把该代码复制到doParseTask方法前,及时返回<br/>
     * 尝试容错,当后台大佬无法保证数据格式统一<br/>
     * 比如,成功时<br/>
     * {data:{"id":"100"}}<br/>
     * 失败时<br/>
     * {data:[]}<br/>
     * data成功时是对象,失败了是数组<br/>
     * 以下代码仅供参考<br/>
     */
    public void faultTolerance(String resp) {
        try {
            long start = System.currentTimeMillis();
            JSONObject json = new JSONObject(resp);
            int code = json.optInt(CODE);
            String msg = json.optString(MSG);
            boolean error = false;
            JSONObject obj = json.optJSONObject(DATA);
            JSONArray array = null;
            if (obj == null) {
                array = json.optJSONArray(DATA);
                if (array == null) {
                    error = true;
                }
            }
            String data = "";
            if (obj == null || error) {
                data = json.optString(DATA);
                if (TextUtils.isEmpty(data)) {
                    error = true;
                } else {
                    if (error) {
                        error = false;
                    }
                }
            }
            long end = System.currentTimeMillis();
            Logger.e("RESP", "#错误状态#" + error);
            Logger.e("RESP", "##手动解析耗时##" + (end - start) + "##毫秒##" + (end - start) / 1000
                    + "##秒##");
            if (code != SUCCESS || error) {
                // Throwable e = new Exception("错误代码:" + code + "\n错误信息:" + msg);
                Throwable e = new Exception("" + msg);
                ApiException ex = new ApiException(e, code, msg);
                execErrorCallback(ex, mCallback, mLoading);
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
