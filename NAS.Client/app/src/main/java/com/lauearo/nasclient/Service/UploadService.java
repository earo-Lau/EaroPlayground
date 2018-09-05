package com.lauearo.nasclient.Service;

import NAS.Model.UploadModelOuterClass.StreamingNode;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.util.SparseArray;
import android.widget.Toast;
import com.google.protobuf.ByteString;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Model.UploadingViewModel;
import com.lauearo.nasclient.Provider.HttpTaskProvider.IHttpTaskProvider;
import com.lauearo.nasclient.Provider.HttpTaskProvider.ITaskCallBack;
import com.lauearo.nasclient.Provider.HttpTaskProvider.PostProtoBufTask;
import com.lauearo.nasclient.Provider.ModelProvider.CacheViewModelProvider;
import com.lauearo.nasclient.Provider.ModelProvider.IStreamingNodeProvider;
import com.lauearo.nasclient.Provider.ModelProvider.IStreamingNodeProviderImpl;
import com.lauearo.nasclient.Provider.ModelProvider.IUploadViewModelProvider;
import com.lauearo.nasclient.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static NAS.Model.UploadModelOuterClass.UploadModel;

public class UploadService extends IntentService {
    private static final String TAG = "UploadService";

    //region Field(s)
    private SparseArray<UploadService> mUploadingTask;
    private IUploadViewModelProvider mUploadModelProvider;
    //endregion

    //region Constructor(s)
    public UploadService() {
        super(TAG);
        mUploadModelProvider = CacheViewModelProvider.getInstance();
        mUploadingTask = new SparseArray<>();
    }

    public static Intent newIntent(Context context){
       return new Intent(context, UploadService.class);
    }

    //endregion

    //region Method(s)

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        assert cm != null;
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()){
            Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_LONG).show();

            return;
        }

    }

    public void newUpload(Context ctx, UploadingViewModel viewModel) throws IOException {

    }

    //endregion


    private UploadModel buildUploadModel(ContentResolver contentResolver, Uri fileUri) {
        Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        cursor.close();

        UploadModel.Builder builder = UploadModel.newBuilder();
        builder.setName(fileName);
        builder.setLength(size);

        return builder.build();
    }

    class UploadTask {
        private final Context mContext;
        private final Uri mFileUri;
        private final List<IHttpTaskProvider> mTaskList = new LinkedList<>();
        private UploadModel mUploadModel;

        private ITaskCallBack<UploadModel> mCreateCallback;
        private ITaskCallBack<StreamingNode> mProgressCallback;
        private ITaskCallBack<UploadModel> mDoneCallback;


        UploadTask(Context ctx, Uri fileUri) {
            mContext = ctx;
            mFileUri = fileUri;

            mUploadModel = buildUploadModel(mContext.getContentResolver(), mFileUri);
        }

        UploadModel getUploadModel() {
            return mUploadModel;
        }

        UploadTask setOnCreated(ITaskCallBack<UploadModel> createCallback) {
            this.mCreateCallback = createCallback;
            return this;
        }

        UploadTask setOnProgressUpdate(ITaskCallBack<StreamingNode> onPregressUpdate) {
            this.mProgressCallback = onPregressUpdate;
            return this;
        }

        UploadTask setOnDone(ITaskCallBack<UploadModel> doneCallback) {
            this.mDoneCallback = doneCallback;
            return this;
        }


        void invoke() throws IOException {
            IHttpTaskProvider<UploadModel> newUploadTask =
                    new PostProtoBufTask<>(Constants.NAS_SERVER_URL + "/api" + "/upload/create");
            mTaskList.add(newUploadTask);

            newUploadTask.send(mUploadModel, new ITaskCallBack<UploadModel>() {
                @Override
                public UploadModel onSerializable(InputStream inputStream) throws IOException {
                    return UploadModel.parseFrom(inputStream);
                }

                @Override
                public void onSuccess(UploadModel uploadModel) {
                    mUploadModel = uploadModel;
                    mTaskList.remove(newUploadTask);

                    performUpload();
                    if (mCreateCallback != null) {
                        mCreateCallback.onSuccess(uploadModel);
                    }
                }

                @Override
                public void onFailure(Exception exceptions) {
                    String errMsg = String.format(mContext.getString(R.string.upload_create_failed),
                            mUploadModel.getName());
                    Toast.makeText(mContext, errMsg, Toast.LENGTH_LONG).show();
                    mTaskList.remove(newUploadTask);

                    if (mCreateCallback != null) {
                        mCreateCallback.onFailure(exceptions);
                    }
                }

                @Override
                public void onCancel() {
                    mTaskList.remove(newUploadTask);

                    if (mCreateCallback != null) {
                        mCreateCallback.onCancel();
                    }
                }
            });
        }

        void cancelTask() {
            for (IHttpTaskProvider task : mTaskList) {
                task.cancelTask();
            }
        }

        @SuppressLint("StaticFieldLeak")
        private void performUpload() {
            StreamingNode root = mUploadModel.getRoot();
            IStreamingNodeProvider nodeProvider = new IStreamingNodeProviderImpl(root);
            Collection<StreamingNode> streamingNodes = new LinkedList<>();

            while (nodeProvider.hasNext()) {
                StreamingNode node = nodeProvider.next();
                streamingNodes.add(node);
            }

            new Thread(() -> {
                nodeProvider.reset();

                while (!streamingNodes.isEmpty()) {
                    if (mTaskList.size() > 4) {
                        try {
                            Thread.sleep(2000);
                            continue;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    if (nodeProvider.hasNext()) {
                        try {
                            IHttpTaskProvider<StreamingNode> streamingTask =
                                    new PostProtoBufTask<>(Constants.NAS_SERVER_URL + "/api/upload/stream");
                            mTaskList.add(streamingTask);

                            InputStream inputStream = mContext.getContentResolver().openInputStream(mFileUri);
                            assert inputStream != null;
                            ByteString bytes = ByteString.readFrom(inputStream, 200000);

                            StreamingNode node = nodeProvider.next();
                            StreamingNode uploadingStream = node.toBuilder()
                                                                .setStream(bytes)
                                                                .setUploadModle(mUploadModel.getId())
                                                                .build();

                            streamingTask.send(uploadingStream, new ITaskCallBack<StreamingNode>() {
                                @Override
                                public void onSuccess(String resultString) {
                                    mTaskList.remove(streamingTask);
                                    streamingNodes.remove(node);

                                    if (resultString.equalsIgnoreCase("ok") && mProgressCallback != null) {
                                        mProgressCallback.onSuccess(uploadingStream);
                                    }
                                }

                                @Override
                                public void onFailure(Exception exceptions) {
                                    mTaskList.remove(streamingTask);
                                    streamingNodes.remove(uploadingStream);

                                    if (mProgressCallback != null) {
                                        mProgressCallback.onFailure(exceptions);
                                    }
                                }

                                @Override
                                public void onCancel() {
                                    mTaskList.remove(streamingTask);
                                    streamingNodes.clear();

                                    if (mProgressCallback != null) {
                                        mProgressCallback.onCancel();
                                    }
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                uploadingDone();
            }).run();
        }

        private void uploadingDone() {
            try {
                IHttpTaskProvider<UploadModel> doneTask =
                        new PostProtoBufTask<UploadModel>(Constants.NAS_SERVER_URL + "/api/upload/done");

                doneTask.send(mUploadModel, new ITaskCallBack<UploadModel>() {
                    @Override
                    public void onSuccess(String resultString) {
                        if (resultString.equals("ok") && mDoneCallback != null) {
                            mDoneCallback.onSuccess(mUploadModel);
                        }
                    }

                    @Override
                    public void onFailure(Exception exceptions) {
                        if (mDoneCallback != null) {
                            mDoneCallback.onFailure(exceptions);
                        }
                    }

                    @Override
                    public void onCancel() {
                        if (mDoneCallback != null) {
                            mDoneCallback.onCancel();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
