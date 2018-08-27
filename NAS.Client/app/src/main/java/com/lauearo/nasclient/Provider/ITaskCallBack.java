package com.lauearo.nasclient.Provider;

public interface ITaskCallBack<T> {
    void onSuccess(T entity);
    void onFailure(Exception exceptions);
    void onCancel();
}
