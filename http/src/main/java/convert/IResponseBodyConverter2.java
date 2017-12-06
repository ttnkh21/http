package convert;

import com.vise.log.Logger;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Converter;

public class IResponseBodyConverter2 implements Converter<ResponseBody, String> {

    private static final String TAG = "IResponseBodyConverter2";

    @Override
    public String convert(ResponseBody value) throws IOException {
        String string = value.string();
//		if (value instanceof ResponseBodyWrap) {
//			System.out.println("#下载/上传#" + string);
//		} else {
        System.out.println("#Resp转换#" + value);
        System.out.println("#Resp转换#" + string);
        Logger.e(TAG, "#解密前@#" + string);
//			// string = AesEncryptionUtil.decrypt(string);
        Logger.e(TAG, "#解密后@#" + string);
//		}
        return string;
    }
}
