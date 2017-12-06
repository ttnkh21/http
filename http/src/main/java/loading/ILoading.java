package loading;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * 时间：2017/12/5 下午4:40
 */

public class ILoading extends AlertDialog implements ILoadingI {
    public ILoading(@NonNull Context context) {
        super(context);
    }

    @Override
    public void beginShow() {
        show();
    }

    @Override
    public void beginDismiss() {
        dismiss();
    }

    @Override
    public boolean isShowingI() {
        return isShowing();
    }
}
