package com.lauearo.nasclient.Provider.HttpTaskProvider;

public interface IHttpTaskProvider<T> {
    void send(T entity, ITaskCallBack<T> callBack);
    void cancelTask();
}
