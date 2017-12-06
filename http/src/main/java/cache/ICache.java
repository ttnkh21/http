package cache;

/**
 * 作者：马俊
 * 时间：2017/12/5 下午3:19
 * 邮箱：747673016@qq.com
 */
public interface ICache {
    void put(String key, Object value);

    Object get(String key);

    boolean contains(String key);

    void remove(String key);

    void clear();
}