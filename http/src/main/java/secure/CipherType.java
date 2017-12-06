package secure;

/**
 * 作者：马俊
 * 时间：2017/8/10 下午3:21
 * 邮箱：747673016@qq.com
 */

public enum CipherType {
    SHA("SHA"),
    MD5("MD5"),
    Hmac_MD5("HmacMD5"),
    Hmac_SHA1("HmacSHA1"),
    Hmac_SHA256("HmacSHA256"),
    Hmac_SHA384("HmacSHA384"),
    Hmac_SHA512("HmacSHA512"),
    DES("DES"),
    RSA("RSA");

    private String type;

    CipherType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
