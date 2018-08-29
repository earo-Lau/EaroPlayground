package com.lauearo.nasclient.Provider.ModelProvider;

import NAS.Model.UploadModelOuterClass;
import android.content.res.Resources;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Model.UploadingViewModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class CacheViewModelProvider implements IUploadViewModelProvider {
    //region Singleton
    private static CacheViewModelProvider sInstance;
    private List<UploadingViewModel> mUploadList;

    //region Constructor(s)
    private CacheViewModelProvider() {
        mUploadList = new LinkedList<>();

        Random r = new Random(10000);
        UploadModelOuterClass.UploadModel.Builder builder = UploadModelOuterClass.UploadModel.newBuilder();
        for (int i = 0; i < 5; i++) {
            builder.setId(i + "-" + UUID.randomUUID());
            builder.setName("Sample-" + i);
            builder.setLength(r.nextInt());

            newUploadModel(builder.build());
        }
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
    public UploadingViewModel getUploadingViewModel(String id) {
        for (UploadingViewModel model : mUploadList) {
            if (model.getUploadModel().getId().equalsIgnoreCase(id)) {
                return model;
            }
        }

        throw new Resources.NotFoundException(String.format("id %s not found ", id));
    }

    @Override
    public UploadingViewModel newUploadModel(UploadModelOuterClass.UploadModel uploadModel) {
        UploadingViewModel vm = new UploadingViewModel(uploadModel);
        vm.setStatus(Constants.UPLOADING_STATUS_PAUSE);
        vm.setProgress(0);

        this.mUploadList.add(vm);
        return vm;
    }

    public UploadingViewModel rmUploadModel(UploadModelOuterClass.UploadModel uploadModel) {
        UploadingViewModel targetModel = null;
        for (UploadingViewModel model : mUploadList) {
            if (model.getUploadModel().getId().equalsIgnoreCase(uploadModel.getId())) {
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
