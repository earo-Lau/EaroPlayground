package com.lauearo.nasclient.Provider;

public interface IHttpTaskProvider<T> {
    void send(T entity, ITaskCallBack<T> callBack);
}
