package callback;

/**
 * 时间：2017/12/5 下午3:41
 */

public interface UCallback {
    void onProgress(long currentLength, long totalLength, float percent);

    void onFail(int errCode, String errMsg);
}
