package api;


import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 通用请求封装<br/>
 */
public interface Api {
    String KEY = "data";
    String PATH = "path";

    @FormUrlEncoded
    @POST("{path}")
    Observable<String> runPost(@Path(value = PATH, encoded = true) String path,
                               @Field(KEY) String json);

    @GET("{path}")
    Observable<String> runGet(@Path(value = PATH, encoded = true) String path,
                              @Query(KEY) String json);

    @Streaming
    @GET
    Observable<ResponseBody> downFile(@Url() String url, @QueryMap Map<String, String> maps);

    @Multipart
    //@POST("/")
    @POST
    Observable<ResponseBody> uploadFiles(@Url() String url, @Part() List<MultipartBody.Part> parts);
}
