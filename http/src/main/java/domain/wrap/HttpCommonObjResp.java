package domain.wrap;

/**
 * 作者：马俊
 * 时间：2017/12/5 下午3:59
 * 邮箱：747673016@qq.com
 */

public class HttpCommonObjResp<T> extends CommonResp {
    private static final long serialVersionUID = 1L;
    public T data;

    public T getData() {
        return data == null ? null: data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "HttpCommonObjResp{" +
                "data=" + data +
                "} " + super.toString();
    }
}
