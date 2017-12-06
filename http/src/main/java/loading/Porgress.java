package loading;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * 时间：2017/12/5 下午4:30
 */

public class Porgress extends ProgressDialog implements ILoadingI {
    public Porgress(Context context) {
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
