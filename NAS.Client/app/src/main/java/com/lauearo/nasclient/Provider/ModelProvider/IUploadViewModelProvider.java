package com.lauearo.nasclient.Provider.ModelProvider;

import NAS.Model.UploadModelOuterClass;
import com.lauearo.nasclient.Model.UploadingViewModel;

import java.util.List;

public interface IUploadViewModelProvider {
    List<UploadingViewModel> getUploadingList();
    UploadingViewModel getUploadingViewModel(String id);

    UploadingViewModel newViewModel(UploadModelOuterClass.UploadModel uploadModel);
    UploadingViewModel rmViewModel(String id);
}
