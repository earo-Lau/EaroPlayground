package com.lauearo.nasclient.Provider.ModelProvider;

import NAS.Model.UploadModelOuterClass;
import com.lauearo.nasclient.Model.UploadingViewModel;

import java.util.List;

public interface IUploadViewModelProvider {
    List<UploadingViewModel> getUploadingList();
    UploadingViewModel getUploadingViewModel(String id);

    UploadingViewModel newUploadModel(UploadModelOuterClass.UploadModel uploadModel);
    UploadingViewModel rmUploadModel(UploadModelOuterClass.UploadModel uploadModel);
}
