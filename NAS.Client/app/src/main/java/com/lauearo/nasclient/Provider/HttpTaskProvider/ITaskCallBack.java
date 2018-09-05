package com.lauearo.nasclient.Provider.HttpTaskProvider;

import java.io.IOException;
import java.io.InputStream;

public interface ITaskCallBack<T> {
    default T onSerializable(InputStream inputStream) throws IOException {
        return null;
    }

    default void onSuccess(T entity) {
        throw new ClassCastException();
    }

    default void onSuccess(String resultString) {
        
    }

    default void onFailure(Exception exceptions) {
    }

    default void onCancel() {
    }
}
