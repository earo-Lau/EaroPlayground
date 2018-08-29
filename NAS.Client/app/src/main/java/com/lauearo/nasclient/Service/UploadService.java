package com.lauearo.nasclient.Service;

import NAS.Model.UploadModelOuterClass;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
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
import java.util.HashMap;
import java.util.List;

public class UploadService {
    //endregion
    //region Instance
    private static UploadService sInstance;
    private static HashMap<String, IHttpTaskProvider> sUploadingTask;
    private IUploadViewModelProvider mUploadModelProvider;
    //region Field(s)

    //region Constructor(s)
    private UploadService() {
        mUploadModelProvider = CacheViewModelProvider.getInstance();
        sUploadingTask = new HashMap<>();
    }

    //endregion

    public static UploadService getInstance() {
        if (sInstance == null) {
            sInstance = new UploadService();
        }

        return sInstance;
    }
    //endregion

    //region Properties

    public List<UploadingViewModel> getUploadingViewModels() {
        return mUploadModelProvider.getUploadingList();
    }
    //endregion

    //region Method(s)
    public void beginUpload(Context ctx, Uri fileUri) throws IOException {
        UploadModelOuterClass.UploadModel uploadModel = buildUploadModel(ctx.getContentResolver(), fileUri);
        UploadingViewModel vm = mUploadModelProvider.newUploadModel(uploadModel);

        IHttpTaskProvider<UploadModelOuterClass.UploadModel> newUploadTask =
                new PostProtoBufTask<>("http://192.168" + ".43.117:8073/api/upload/create");
        sUploadingTask.put(uploadModel.getId(), newUploadTask);

        newUploadTask.send(uploadModel, new ITaskCallBack<UploadModelOuterClass.UploadModel>() {
            @Override
            public void onSuccess(UploadModelOuterClass.UploadModel entity) {
                performUpload(ctx, fileUri, vm);
            }

            @Override
            public void onFailure(Exception exceptions) {
                String errMsg = String.format(ctx.getString(R.string.upload_create_failed), uploadModel.getName());
                Toast.makeText(ctx, errMsg, Toast.LENGTH_LONG).show();

                vm.setStatus(Constants.UPLOADING_STATUS_FAILURE);
            }

            @Override
            public void onCancel() {
                mUploadModelProvider.rmUploadModel(uploadModel).cancel();
            }
        });
    }

    private void performUpload(Context ctx, Uri fileUri, UploadingViewModel viewModel) {
        if (fileUri == null) {
            Toast.makeText(ctx, String.format(ctx.getString(R.string.content_uri_not_found),
                    viewModel.getUploadModel().getName()), Toast.LENGTH_LONG).show();

        }
    }

    public void cancelUpload(UploadingViewModel viewModel) {
        IHttpTaskProvider taskProvider = sUploadingTask.get(viewModel.getUploadModel().getId());

        if (taskProvider != null) {
            taskProvider.cancelTask();
        } else {
            mUploadModelProvider.rmUploadModel(viewModel.getUploadModel()).cancel();
        }
    }

    private UploadModelOuterClass.UploadModel buildUploadModel(ContentResolver contentResolver, Uri fileUri) {
        Cursor cursor = contentResolver.query(fileUri, null, null, null, null);
        assert cursor != null;
        cursor.moveToFirst();
        String fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
        long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
        cursor.close();

        UploadModelOuterClass.UploadModel.Builder builder = UploadModelOuterClass.UploadModel.newBuilder();
        builder.setName(fileName);
        builder.setLength(size);

        return builder.build();
    }

    //endregion
}
