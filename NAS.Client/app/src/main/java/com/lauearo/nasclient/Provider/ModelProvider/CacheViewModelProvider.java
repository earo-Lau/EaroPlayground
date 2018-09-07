package com.lauearo.nasclient.Provider.ModelProvider;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Model.UploadingViewModel;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static NAS.Model.UploadModelOuterClass.UploadModel;

public class CacheViewModelProvider implements IUploadViewModelProvider {
    //region Singleton
    private static CacheViewModelProvider sInstance;
    private List<UploadingViewModel> mUploadList;

    //region Constructor(s)
    private CacheViewModelProvider() {
        mUploadList = new LinkedList<>();

        /*Random r = new Random(10000);
        UploadModel.Builder builder = UploadModel.newBuilder();
        for (int i = 0; i < 5; i++) {
            builder.setId(i + "-" + UUID.randomUUID());
            builder.setName("Sample-" + i);
            builder.setLength(r.nextInt());

            UploadingViewModel vm = new UploadingViewModel(builder.build());
            vm.setStatus(Constants.UPLOADING_STATUS_PENDING);

            this.mUploadList.add(vm);
        }*/
    }
    //endregion

    public static CacheViewModelProvider getInstance() {
        if (sInstance == null) {
            sInstance = new CacheViewModelProvider();
        }
        return sInstance;
    }
    //endregion


    //region IUploadViewModelProvider.Method
    @Override
    public List<UploadingViewModel> getUploadingList() {
        return mUploadList;
    }

    @Override
    public Iterable<UploadingViewModel> getPendingList() {
        return mUploadList.stream()
                          .filter(viewModel -> viewModel.getStatus() == Constants.UPLOADING_STATUS_PENDING)
                          .collect(Collectors.toList());
    }

    @Override
    public UploadingViewModel getUploadingViewModel(String id) {
        for (UploadingViewModel model : mUploadList) {
            if (model.getUploadModel().getId().equalsIgnoreCase(id)) {
                return model;
            }
        }

        throw new Resources.NotFoundException(String.format("id %s not found ", id));
    }

    @Override
    public UploadingViewModel newUpload(ContentResolver contentResolver, Uri fileUri) {
        UploadModel uploadModel = buildUploadModel(contentResolver, fileUri);
        UploadingViewModel vm = new UploadingViewModel(uploadModel);

        vm.setStatus(Constants.UPLOADING_STATUS_PENDING);
        vm.setProgress(0);
        vm.setFileUri(fileUri);

        this.mUploadList.add(vm);
        return vm;
    }

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

    @Override
    public UploadingViewModel rmViewModel(String id) {
        UploadingViewModel targetModel = null;
        for (UploadingViewModel model : mUploadList) {
            if (model.getUploadModel().getId().equalsIgnoreCase(id)) {
                targetModel = model;
                break;
            }
        }

        if (targetModel != null) {
            mUploadList.remove(targetModel);
        }

        return targetModel;
    }
    //endregion
}
