package domain.wrap;

import java.io.Serializable;

/**
 * 作者：马俊
 * 时间：2017/12/5 下午3:56
 * 邮箱：747673016@qq.com
 */

public class CommonResp implements Serializable {
    public static final long serialVersionUID = 1L;
    public static final String KEY_MSG = "msg";
    public static final String KEY_CODE = "code";
    public int code;
    public String msg;

    @Override
    public String toString() {
        return "CommonResp [code=" + code + ", msg=" + msg
                + "]";
    }

    public boolean isSuccess() {
        return code == 1; // 1 成功 -1 失败 -2 退出登录
    }
}

