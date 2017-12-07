package http.com.tst;

import api.ObtainPath;
import domain.wrap.CommonRequest;

/**
 * 时间：2017/12/7 下午3:49
 */
@ObtainPath("user/userLogin")
public class Login extends CommonRequest {
    public String account;
    public String password;
}
