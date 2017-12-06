package interceptor;

import android.support.annotation.NonNull;

import java.io.IOException;

import callback.UCallback;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * @Description: 下注进度拦截
 */
public class DownLoadProgressInterceptor implements Interceptor {

    private UCallback callback;

    public DownLoadProgressInterceptor(UCallback callback) {
        this.callback = callback;
        if (callback == null) {
            throw new NullPointerException("this callback must not null.");
        }
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        // 拦截
        Response originalResponse = chain.proceed(chain.request());
        System.out.println("##下载进度拦截执行##");
        // 包装响应体并返回
        return originalResponse
                .newBuilder()
                .body(new DownloadProgressRespBody(originalResponse.body(),
                        callback)).build();
    }
}
