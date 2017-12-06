package http.com.tst;

import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * 作者：马俊
 * 时间：2017/12/6 下午5:19
 * 邮箱：747673016@qq.com
 */

public class ClickHelper {
    /**
     * 防止快速点击
     *
     * @param okTv
     */
    public static void helper(View okTv, final CommonCallBackII mCommonCallBackII) {
        System.out.println("#点击事件包裹#" + okTv);
        RxView.clicks(okTv)
                .throttleFirst(2, TimeUnit.SECONDS)   //两秒钟之内只取一个点击事件，防抖操作
                .subscribe(new Consumer<Object>() {

                    @Override
                    public void accept(Object o) throws Exception {
                        if (mCommonCallBackII != null) {
                            System.out.println("#事件回掉#");
                            mCommonCallBackII.doCallback();
                        }
                    }
                });
    }
}
