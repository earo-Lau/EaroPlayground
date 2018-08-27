package com.lauearo.nasclient.Service;

import NAS.Model.UploadModelOuterClass;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.widget.Toast;
import com.lauearo.nasclient.Provider.IHttpTaskProvider;
import com.lauearo.nasclient.Provider.ITaskCallBack;
import com.lauearo.nasclient.Provider.PostProtoBufTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadService {
    //endregion
    //region Instance
    private static UploadService _instance;
    //region Field(s)
    private List<UploadModelOuterClass.UploadModel> _uploadList;

    //region Constructor(s)
    private UploadService() {
        _uploadList = new ArrayList<>();

    }

    //endregion

    public static UploadService getInstance() {
        if (_instance == null) {
            _instance = new UploadService();
        }

        return _instance;
    }

    //endregion

    //region Properties
    public List<UploadModelOuterClass.UploadModel> get_uploadList() {
        return _uploadList;
    }

    public void set_uploadList(List<UploadModelOuterClass.UploadModel> _uploadList) {
        this._uploadList = _uploadList;
    }
    //endregion

    //region Method(s)
    public void beginUpload(Context ctx, Uri fileUri) throws IOException {
        UploadModelOuterClass.UploadModel uploadModel = buildUploadModel(ctx.getContentResolver(), fileUri);

        IHttpTaskProvider<UploadModelOuterClass.UploadModel> newUploadTask = new PostProtoBufTask<>("http://192.168.43.117:8073/api/upload/create");

        newUploadTask.send(uploadModel, new ITaskCallBack<UploadModelOuterClass.UploadModel>() {
            @Override
            public void onSuccess(UploadModelOuterClass.UploadModel entity) {
                Toast.makeText(ctx, "create success", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception exceptions) {

            }

            @Override
            public void onCancel() {

            }
        });
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
