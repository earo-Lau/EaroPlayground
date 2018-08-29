package com.lauearo.nasclient.Model;

import NAS.Model.UploadModelOuterClass;

import java.util.EventListener;
import java.util.EventObject;
import java.util.LinkedList;
import java.util.List;

public class UploadingViewModel {

    private UploadModelOuterClass.UploadModel mUploadModel;
    private int mStatus;
    private int mProgress;
    private List<StatusUpdateEventListener> mStatusUpdateEventListeners;
    private List<ProgressUpdateEventListener> mProgressUpdateEventListeners;
    private List<CancelEventListener> mCancelEventListeners;

    public UploadingViewModel(UploadModelOuterClass.UploadModel uploadModel) {
        mUploadModel = uploadModel;

        mStatusUpdateEventListeners = new LinkedList<>();
        mProgressUpdateEventListeners = new LinkedList<>();
        mCancelEventListeners = new LinkedList<>();
    }

    public UploadModelOuterClass.UploadModel getUploadModel() {
        return mUploadModel;
    }

    public int getStatus() {
        return mStatus;
    }

    public void setStatus(int status) {
        mStatus = status;
        if (mStatusUpdateEventListeners != null) {
            mStatusUpdateEventListeners.forEach((action) -> action.onUpdate(this.getStatus(), new EventObject(this)));
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        if (mProgressUpdateEventListeners != null) {
            int percentage = Math.round((float) mProgress / mUploadModel.getLength());
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

    public void cancel() {
        if (mCancelEventListeners != null) {
            mCancelEventListeners.forEach((action) -> action.onCancelled(new EventObject(this)));
        }
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
