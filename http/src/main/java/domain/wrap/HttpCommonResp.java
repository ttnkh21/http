package domain.wrap;

import java.util.ArrayList;
import java.util.List;

/**
 * 时间：2017/12/5 下午3:59
 */

public class HttpCommonResp<T> extends CommonResp {
    private static final long serialVersionUID = 1L;
    public List<T> data;

    public List<T> getDatas() {
        if (data == null) {
            data = new ArrayList<T>();
        }
        return data;
    }
}
