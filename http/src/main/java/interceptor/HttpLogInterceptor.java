package interceptor;

import android.support.annotation.NonNull;

import com.vise.log.Logger;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import common.config.CommonConfig;
import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

/**
 * @Description: Http日志打印拦截
 * @author: <a href="http://www.xiaoyaoyou1212.com">DAWI</a>
 * @date: 2017-04-08 15:16
 */
public class HttpLogInterceptor implements Interceptor {
    private static final Charset UTF8 = Charset.forName("UTF-8");
    private volatile Level level = Level.NONE;

    public enum Level {
        NONE,       //不打印log
        BASIC,      //只打印 请求首行 和 响应首行
        HEADERS,    //打印请求和响应的所有 Header
        BODY        //所有数据全部打印
    }

    private void log(String message) {
        if (CommonConfig.DEBUG) {
            System.out.println(message);
        }
    }

    public HttpLogInterceptor setLevel(Level level) {
        if (level == null) throw new NullPointerException("level == null. Use Level.NONE instead.");
        this.level = level;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }

        //请求日志拦截
        logForRequest(request, chain.connection());

        //执行请求，计算请求时间
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            log("<-- HTTP FAILED: " + e);
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        //响应日志拦截
        return logForResponse(response, tookMs);
    }

    private void logForRequest(Request request, Connection connection) throws IOException {
        boolean logBody = (level == Level.BODY);
        boolean logHeaders = (level == Level.BODY || level == Level.HEADERS);
        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;
        Protocol protocol = connection != null ? connection.protocol() : Protocol.HTTP_1_1;
        StringBuilder sb = new StringBuilder();
        String host = "";
        try {
            String source = request.url().toString();
            host = source.substring(source.indexOf(CommonConfig.API_HOST_VERSION) + CommonConfig.API_HOST_VERSION.length());
            sb.append("请求前缀:" + host);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String requestStartMessage = "--> " + request.method() + ' ' + request.url() + ' ' + protocol;
            log(requestStartMessage);
            sb.append("\n请求信息:" + requestStartMessage);
            if (logHeaders) {
                Headers headers = request.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    log("\t" + headers.name(i) + ": " + headers.value(i));
                    sb.append("\n" + headers.name(i) + ": " + headers.value(i));
                }

                log(" ");
                sb.append("\n ");
                if (logBody && hasRequestBody) {
                    if (isPlaintext(requestBody.contentType())) {
                        log("\t" + requestBody.contentType());
                        sb.append("\n" + requestBody.contentType());
                        sb.append("\nbody:" + bodyToStringii(request));
                    } else {
                        log("\tbody: maybe [file part] , too large too print , ignored!");
                        sb.append("\nbody: maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            log("" + e);
            sb.append("\n" + e);
        } finally {
            log("--> END " + request.method());
            sb.append("\n--> END " + request.method());
        }
        Logger.e(host, sb.toString());
    }

    private Response logForResponse(Response response, long tookMs) {
        Response.Builder builder = response.newBuilder();
        Response clone = builder.build();
        ResponseBody responseBody = clone.body();
        boolean logBody = (level == Level.BODY);
        boolean logHeaders = (level == Level.BODY || level == Level.HEADERS);
        StringBuilder sb = new StringBuilder();
        String host = "";
        try {
            String source = clone.request().url().toString();
            host = source.substring(source.indexOf(CommonConfig.API_HOST_VERSION) + CommonConfig.API_HOST_VERSION.length());
            sb.append("请求前缀:" + host);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            log("<-- " + clone.code() + ' ' + clone.message() + ' ' + clone.request().url() + " (" + tookMs + "ms）");
            sb.append("\n<-- " + clone.code() + ' ' + clone.message() + ' ' + clone.request().url() + " (" + tookMs + "ms）");
            if (logHeaders) {
                Headers headers = clone.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    log("\t" + headers.name(i) + ": " + headers.value(i));
                    sb.append("\n" + headers.name(i) + ": " + headers.value(i));
                }
                log(" ");
                sb.append("\n");
                if (logBody && HttpHeaders.hasVaryAll(clone)) {
                    if (responseBody != null && isPlaintext(responseBody.contentType())) {
                        String body = responseBody.string();
                        log("\tbody:" + body);
                        sb.append("\nbody:" + body);
                        responseBody = ResponseBody.create(responseBody.contentType(), body);
                        Logger.e(host, sb.toString());
                        return response.newBuilder().body(responseBody).build();
                    } else {
                        log("\tbody: maybe [file part] , too large too print , ignored!");
                        sb.append("\nbody: maybe [file part] , too large too print , ignored!");
                    }
                }
            }
        } catch (Exception e) {
            // ViseLog.e(e);
            log(e.toString());
        } finally {
            log("<-- END HTTP");
        }
        Logger.e(host, sb.toString());
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private boolean isPlaintext(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        String subtype = mediaType.subtype();
        if (subtype != null) {
            subtype = subtype.toLowerCase();
            if (subtype.contains("x-www-form-urlencoded") ||
                    subtype.contains("json") ||
                    subtype.contains("xml") ||
                    subtype.contains("html"))
                return true;
        }
        return false;
    }

    public void bodyToString(Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            RequestBody requestBody = copy.body();
            if (requestBody != null) {
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if (charset != null) {
                    log("\tbody:" + URLDecoder.decode(buffer.readString(charset), UTF8.name()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String bodyToStringii(Request request) {
        String body = "";
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            RequestBody requestBody = copy.body();
            if (requestBody != null) {
                requestBody.writeTo(buffer);
                Charset charset = UTF8;
                MediaType contentType = requestBody.contentType();
                if (contentType != null) {
                    charset = contentType.charset(UTF8);
                }
                if (charset != null) {
                    body = URLDecoder.decode(buffer.readString(charset), UTF8.name());
                    log("\tbody:" + body);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }
}
