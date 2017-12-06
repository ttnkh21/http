package convert;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.vise.log.Logger;

import java.io.IOException;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * RequestBody 对请求体进行加密处理
 */
public class IRequestBodyConverter2<T> implements Converter<T, RequestBody> {
    private static final MediaType MEDIA_TYPE = MediaType
            .parse("application/json; charset=UTF-8");
    static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final String TAG = "IRequestBodyConverter2";

    final Gson gson;
    final TypeAdapter<T> adapter;

    IRequestBodyConverter2(Gson gson, TypeAdapter<T> adapter) {
        this.gson = gson;
        this.adapter = adapter;
        //Logger.log(TAG, "#IRequestBodyConverter初始化#");
    }

    @Override
    public RequestBody convert(T value) throws IOException {
        String json = value.toString();
        Logger.e(TAG, "#加密前#" + json);
        System.out.println("#请求转换#" + value);
        //json = AesEncryptionUtil.encrypt(json);
        Logger.e(TAG, "#加密后#" + json);
        return RequestBody.create(MEDIA_TYPE, json);
    }
}
