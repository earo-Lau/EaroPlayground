package com.lauearo.nasclient.Service;

import NAS.Model.UploadModelOuterClass.UploadModel;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Model.UploadingViewModel;
import com.lauearo.nasclient.Provider.HttpTaskProvider.IHttpTaskProvider;
import com.lauearo.nasclient.Provider.HttpTaskProvider.ITaskCallBack;
import com.lauearo.nasclient.Provider.HttpTaskProvider.PostProtoBufTask;
import com.lauearo.nasclient.Provider.ModelProvider.CacheViewModelProvider;
import com.lauearo.nasclient.Provider.ModelProvider.IUploadViewModelProvider;
import com.lauearo.nasclient.R;

import java.io.IOException;
import java.io.InputStream;

public class UploadService extends IntentService {
    private static final String TAG = "UploadService";

    //region Field(s)
    private IUploadViewModelProvider mUploadModelProvider;
    //endregion

    //region Constructor(s)
    public UploadService() {
        super(TAG);
        mUploadModelProvider = CacheViewModelProvider.getInstance();
    }

    public static Intent newIntent(Context context) {
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
        if (!isNetworkAvailableAndConnected()) {
            Toast.makeText(getApplicationContext(), R.string.network_unavailable, Toast.LENGTH_LONG).show();

            return;
        }

        mUploadModelProvider.getPendingList().forEach(viewModel -> {
            try {
                newUpload(viewModel);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

    }

    private void newUpload(UploadingViewModel viewModel) throws IOException {
        IHttpTaskProvider<UploadModel> createFileTask = new PostProtoBufTask<>(Constants.NAS_SERVER_URL + "/api" +
                "/upload/create");
        UploadingViewModel.CancelEventListener cancelEventListener = e -> createFileTask.cancelTask();
        viewModel.addCancelEventListeners(cancelEventListener);

        createFileTask.send(viewModel.getUploadModel(), new ITaskCallBack<UploadModel>() {
            @Override
            public UploadModel onSerializable(InputStream inputStream) throws IOException {
                UploadModel uploadModel = UploadModel.parseFrom(inputStream);
                viewModel.setUploadModel(uploadModel);

                return uploadModel;
            }

            @Override
            public void onSuccess(UploadModel uploadModel) {
                viewModel.setStatus(Constants.UPLOADING_STATUS_PLAY);
                viewModel.rmCancelEventListener(cancelEventListener);

                Intent streamingIntent = StreamingService.newIntent(getApplicationContext());
                streamingIntent.putExtra("id", viewModel.getUploadModel().getId());
                startService(streamingIntent);

                broadActionResult();
            }

            @Override
            public void onFailure(Exception exceptions) {
                viewModel.setStatus(Constants.UPLOADING_STATUS_FAILURE);
                Toast.makeText(getApplicationContext(), R.string.upload_create_failed, Toast.LENGTH_LONG).show();

                broadActionResult();
            }

            @Override
            public void onCancel() {
                viewModel.rmCancelEventListener(cancelEventListener);

                Intent cancelIntent = new Intent(Constants.ACTION_CANCEL);
                cancelIntent.putExtra("viewModelId", viewModel.getUploadModel().getId());

                sendBroadcast(cancelIntent);
            }
        });
    }

    private void broadActionResult() {
        Intent createResult = new Intent(Constants.ACTION_NEW);

        sendBroadcast(createResult);
    }

    //endregion

}
