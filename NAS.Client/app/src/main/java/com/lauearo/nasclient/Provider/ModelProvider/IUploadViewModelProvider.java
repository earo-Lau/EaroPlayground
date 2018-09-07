package com.lauearo.nasclient.Provider.ModelProvider;

import android.content.ContentResolver;
import android.net.Uri;
import com.lauearo.nasclient.Model.UploadingViewModel;

import java.util.List;

@SuppressWarnings("UnusedReturnValue")
public interface IUploadViewModelProvider {
    List<UploadingViewModel> getUploadingList();

    Iterable<UploadingViewModel> getPendingList();

    UploadingViewModel getUploadingViewModel(String id);

    UploadingViewModel newUpload(ContentResolver contentResolver, Uri fileUri);

    UploadingViewModel rmViewModel(String id);
}
