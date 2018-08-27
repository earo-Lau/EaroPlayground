package com.lauearo.nasclient.Model;

public class AsyncTaskResult<T> {
    //region Field(s)

    private T result;
    private Exception exception;
    //endregion


    //region Properties
    public T getResult() {
        return result;
    }

    public Exception getException() {
        return exception;
    }
    //endregion

    //region Constructor(s)

    public AsyncTaskResult(T result) {
        this.result = result;
    }

    public AsyncTaskResult(Exception exception) {
        this.exception = exception;
    }
    //endregion
}
