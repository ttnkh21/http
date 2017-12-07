package http.com.tst;

import api.ObtainPath;
import domain.wrap.CommonRequest;

/**
 * 时间：2017/12/7 下午3:49
 * https://api.douban.com/v2/book/mAutoPrams
 */
@ObtainPath("book/")
public class Books extends CommonRequest {
    public String account;
    public String password;
    //1220563
    public Books(){
        this.mAutoPrams = "1220563";
    }
}
