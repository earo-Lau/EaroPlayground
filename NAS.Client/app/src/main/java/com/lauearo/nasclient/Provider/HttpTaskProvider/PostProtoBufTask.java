package com.lauearo.nasclient.Provider.HttpTaskProvider;

import android.os.AsyncTask;
import com.google.protobuf.GeneratedMessageLite;
import com.lauearo.nasclient.Model.AsyncTaskResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PostProtoBufTask<T extends GeneratedMessageLite> extends AsyncTask<T, Integer, AsyncTaskResult<T>> implements IHttpTaskProvider<T> {
    private URL mUrl;
    private ITaskCallBack<T> mCallback;

    //region Constructor(s)
    public PostProtoBufTask(String url) throws MalformedURLException {
        this.mUrl = new URL(url);
    }
    //endregion

    @SafeVarargs
    @Override
    protected final AsyncTaskResult<T> doInBackground(T... objects) {
        T entity = objects[0];

        try {
            HttpURLConnection conn = (HttpURLConnection) this.mUrl.openConnection();

            conn.setDoOutput(true);
            conn.setRequestProperty("content-type", "application/x-protobuf");

            OutputStream os = conn.getOutputStream();
            entity.writeTo(os);
            os.close();

            InputStream inputStream = conn.getInputStream();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(conn.getResponseMessage() + " from " + this.mUrl);
            }

            Method parseMethod = entity.getClass().getMethod("parseFrom", InputStream.class);
            T model = (T) parseMethod.invoke(entity, inputStream);

            return new AsyncTaskResult<>(model);
        } catch (IOException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return new AsyncTaskResult<>(e);
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult<T> result) {
        super.onPostExecute(result);
        if (this.mCallback == null) return;

        if (result.getException() != null) {
            this.mCallback.onFailure(result.getException());
        } else if (result.getResult() != null) {
            this.mCallback.onSuccess(result.getResult());
        }
    }

    @Override
    public void send(T entity, ITaskCallBack<T> callBack) {
        this.mCallback = callBack;

        this.execute(entity);
    }

    @Override
    protected void onCancelled() {
        this.mCallback.onCancel();
    }

    @Override
    public void cancelTask() {
        this.cancel(true);
    }

}
