package common.config;

/**
 * 时间：2017/12/5 下午2:19
 * 通用配置<br/>
 */

public class CommonConfig {
    public static boolean DEBUG = true;
    public static final String API_BASE_HOST = !DEBUG ? "http://deve28api.76iw.com/" : "http://e28api.76iw.com/";//默认API主机地址
    public static final String API_HOST_VERSION = "v2/";//版本接口的版本号|| app版本1.3 接口v2
    public static final String API_HOST = API_BASE_HOST + API_HOST_VERSION;//默认API主机地址
    public static final String COOKIE_PREFS = "Cookies_Prefs";//默认Cookie缓存目录

    public static final int DEFAULT_TIMEOUT = 60;//默认超时时间（秒）
    public static final int DEFAULT_MAX_IDLE_CONNECTIONS = 5;//默认空闲连接数
    public static final long DEFAULT_KEEP_ALIVE_DURATION = 8;//默认心跳间隔时长（秒）
    public static final long CACHE_MAX_SIZE = 10 * 1024 * 1024;//默认最大缓存大小（字节）

    public static final int DEFAULT_RETRY_COUNT = 3;//默认重试次数
    public static final int DEFAULT_RETRY_DELAY_MILLIS = 3000;//默认重试间隔时间（毫秒）

    public static final String CACHE_SP_NAME = "sp_cache";//默认SharedPreferences缓存文件名
    public static final String CACHE_DISK_DIR = "disk_cache";//默认磁盘缓存目录
    public static final String CACHE_HTTP_DIR = "http_cache";//默认HTTP缓存目录
    public static final long CACHE_NEVER_EXPIRE = -1;//永久不过期
    public static final int MAX_AGE_ONLINE = 60;//默认最大在线缓存时间（秒）
    public static final int MAX_AGE_OFFLINE = 24 * 60 * 60;//默认最大离线缓存时间（秒）
}