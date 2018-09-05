package com.lauearo.nasclient.Model;

public class AsyncTaskResult<T> {
    //region Field(s)

    private T mResult;
    private Exception mException;
    private String mResultString;
    //endregion


    //region Properties
    public T getResult() {
        return mResult;
    }

    public Exception getException() {
        return mException;
    }
    public String getResultString() {
        return mResultString;
    }
    //endregion

    //region Constructor(s)

    public AsyncTaskResult(T mResult) {
        this.mResult = mResult;
    }

    public AsyncTaskResult(String resultString){
        mResultString = resultString;
    }

    public AsyncTaskResult(Exception exception) {
        this.mException = exception;
    }
    //endregion
}
