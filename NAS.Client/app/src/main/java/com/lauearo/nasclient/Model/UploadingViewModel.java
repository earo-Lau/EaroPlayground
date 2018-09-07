package com.lauearo.nasclient.Model;

import android.net.Uri;

import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

import static NAS.Model.UploadModelOuterClass.UploadModel;

public class UploadingViewModel {
    private UploadModel mUploadModel;
    private int mStatus;
    private long mProgress;
    private Uri fileUri;

    private final List<StatusUpdateEventListener> mStatusUpdateEventListeners;
    private final List<ProgressUpdateEventListener> mProgressUpdateEventListeners;
    private final List<CancelEventListener> mCancelEventListeners;

    public UploadingViewModel(UploadModel uploadModel) {
        mUploadModel = uploadModel;

        mStatusUpdateEventListeners = new LinkedList<>();
        mProgressUpdateEventListeners = new LinkedList<>();
        mCancelEventListeners = new LinkedList<>();
    }

    public UploadModel getUploadModel() {
        return mUploadModel;
    }

    public void setUploadModel(UploadModel uploadModel) {
        mUploadModel = uploadModel;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
        if (!mStatusUpdateEventListeners.isEmpty()) {
            mStatusUpdateEventListeners.forEach((action) -> action.onUpdate(this.getStatus(), new EventObject(this)));
        }
    }

    @SuppressWarnings("unused")
    public long getProgress() {
        return mProgress;
    }

    public void setProgress(long progress) {
        mProgress += progress;
        if (!mProgressUpdateEventListeners.isEmpty()) {
            int percentage = Math.round(mProgress * 100.0f / mUploadModel.getLength());
            mProgressUpdateEventListeners.forEach((action) -> action.onUpdate(percentage, new EventObject(this)));
        }
    }

    public void addStatusUpdateEventListener(StatusUpdateEventListener listener) {
        if (!mStatusUpdateEventListeners.contains(listener)) {
            mStatusUpdateEventListeners.add(listener);
        }
    }

    public void addProgressUpdateEventListener(ProgressUpdateEventListener listener) {
        if (!mProgressUpdateEventListeners.contains(listener)) {
            mProgressUpdateEventListeners.add(listener);
        }
    }

    public void addCancelEventListeners(CancelEventListener listener) {
        if (!mCancelEventListeners.contains(listener)) {
            mCancelEventListeners.add(listener);
        }
    }

    public void rmCancelEventListener(CancelEventListener listener) {
        mCancelEventListeners.remove(listener);
    }

    public void cancel() {
        setStatus(Constants.UPLOADING_STATUS_CANCEL);

        if (!mCancelEventListeners.isEmpty()) {
            mCancelEventListeners.forEach((action) -> action.onCancelled(new EventObject(this)));
        }
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public interface StatusUpdateEventListener extends EventListener {
        void onUpdate(int status, EventObject e);
    }

    public interface ProgressUpdateEventListener extends EventListener {
        void onUpdate(int percentage, EventObject e);
    }

    public interface CancelEventListener extends EventListener {
        void onCancelled(EventObject e);
    }
}
