package callback;

import com.google.gson.internal.$Gson$Types;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import api.ApiException;
import retrofit2.Call;

/**
 * 时间：2017/12/5 下午1:52
 */

public abstract class ResultCallback<T> {
    public Type mType;

    public ResultCallback() {
        mType = getSuperclassTypeParameter(getClass());
    }

    public static Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            throw new RuntimeException("泛型异常");
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        return $Gson$Types
                .canonicalize(parameterized.getActualTypeArguments()[0]);
    }

    // public abstract void onError(Call<T> request, Throwable e);
    public abstract void onError(ApiException e);

    public abstract void onResponse(T response);
}
