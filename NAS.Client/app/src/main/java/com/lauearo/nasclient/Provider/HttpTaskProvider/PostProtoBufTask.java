package com.lauearo.nasclient.Provider.HttpTaskProvider;

import android.os.AsyncTask;
import com.google.protobuf.GeneratedMessageLite;
import com.lauearo.nasclient.Model.AsyncTaskResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PostProtoBufTask<T extends GeneratedMessageLite> extends AsyncTask<T, Integer, AsyncTaskResult> implements IHttpTaskProvider<T> {
    private URL mUrl;
    private ITaskCallBack mCallback;

    //region Constructor(s)
    public PostProtoBufTask(String url) throws MalformedURLException {
        this.mUrl = new URL(url);
    }
    //endregio

    @SafeVarargs
    @Override
    protected final AsyncTaskResult doInBackground(T... objects) {
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

            AsyncTaskResult taskResult = readResponse(inputStream);
            inputStream.close();

            return taskResult;
        } catch (IOException e) {
            e.printStackTrace();
            return new AsyncTaskResult(e);
        }
    }

    private AsyncTaskResult readResponse(InputStream inputStream) throws IOException {
        Object obj = this.mCallback.onSerializable(inputStream);
        if (obj != null) {
            return new AsyncTaskResult(obj);
        } else {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return new AsyncTaskResult(result.toString("UTF-8"));
        }
    }

    @Override
    protected void onPostExecute(AsyncTaskResult result) {
        super.onPostExecute(result);
        if (this.mCallback == null) return;

        if (null != result.getException()) {
            this.mCallback.onFailure(result.getException());
        } else {
            try {
                if (null != result.getResult()) {
                    this.mCallback.onSuccess(result.getResult());
                } else {
                    this.mCallback.onSuccess(result.getResultString());
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.mCallback.onFailure(e);
            }
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
