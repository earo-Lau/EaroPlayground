package com.lauearo.nasclient.Provider.HttpTaskProvider;

public interface ITaskCallBack<T> {
    void onSuccess(T entity);
    void onFailure(Exception exceptions);
    void onCancel();
}
