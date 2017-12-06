rxjava2+okhttp3+retrofit2 组合的一种方式

https://jitpack.io/#majunm/http/v1  使用方式

http://www.baidu.com?data=json 可以调用该形式的接口

UpdateRequest 必须继承 CommonRequest
        |@ObtainPath("version/version") 请求后缀填入注解


1.返回String,自己手动解析

        Porgress mPorgress = new Porgress(this);
        mPorgress.setProgress(100);
        mPorgress.setMessage("加载呢");
        HttpRequestFactory.doPost(new UpdateRequest(), new ResultCallbackAdapterIs<String>(this) {
            @Override
            public void doOnError(ApiException ex) {
                super.doOnError(ex);

            }

            @Override
            public void doOnResponse(String response) {
                super.doOnResponse(response);
                // 自己手动解析
            }
        }, mPorgress);

2.根据接口返回实体对象

            ILoading ll = new ILoading(this);
            ll.setMessage("网络请求");
            // data 是 对象 , 泛型是 HttpCommonObjResp<UpdateResp>
            // data 是 数组 , 泛型是 HttpCommonResp<UpdateResp>
            // 要求返回格式一致,否则请在CommonRespWrapI->onNext(String resp) 处理 参考代码 faultTolerance(String resp);
            HttpRequestFactory.doPost(new UpdateRequest(), new ResultCallbackAdapterIs<HttpCommonObjResp<UpdateResp>>(this) {
                @Override
                public void doOnError(ApiException ex) {
                    super.doOnError(ex);
                    //mTst.setText(ex + "#error#");
                }

                @Override
                public void doOnResponse(HttpCommonObjResp<UpdateResp> response) {
                    super.doOnResponse(response);
                    if (response.isSuccess()) {
                        UpdateResp resp = response.getData();
                        //mTst.setText(resp + "");
                    } else {
                        //mTst.setText(response + "#结果#");
                    }
                }
            }, ll);

=======

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

=======