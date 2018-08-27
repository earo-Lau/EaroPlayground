package com.lauearo.nasclient.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

public class FileLoader {
    /**
     * @param filePath File Path
     * @return the length of file
     */
    //region Method(s)
    public static long getFileSize(String filePath) throws IOException {
        File srcFile = new File(filePath);
        if (!srcFile.exists() || !srcFile.canRead()) {
            throw new IOException("file is not Readable");
        }

        return srcFile.getTotalSpace();
    }

    public static String getFilePath(Context ctx, Uri uri) {
        String path = "";
        if (ctx != null && uri != null) {
            String schema = uri.getScheme();

            if ("content".equalsIgnoreCase(schema)) {
                path = getPathFromContent(ctx.getContentResolver(), uri, null);
            } else if ("file".equalsIgnoreCase(schema)) {
                path = uri.getPath();
            } else if (DocumentsContract.isDocumentUri(ctx, uri)) {
                path = getPathFromDoc(ctx.getContentResolver(), uri);
            }
        }

        return path;
    }

    private static String getPathFromContent(ContentResolver contentResolver, Uri uri, String whereClause) {
        /* Return uri represented document file real local path.*/
        String ret = "";

        // Query the uri with condition.
        Cursor cursor = contentResolver.query(uri, null, whereClause, null, null);

        if (cursor != null) {
            boolean moveToFirst = cursor.moveToFirst();
            if (moveToFirst) {
                // Get columns name by uri type.
                String columnName = MediaStore.Images.Media.DATA;

                if (uri == MediaStore.Images.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Images.Media.DATA;
                } else if (uri == MediaStore.Audio.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Audio.Media.DATA;
                } else if (uri == MediaStore.Video.Media.EXTERNAL_CONTENT_URI) {
                    columnName = MediaStore.Video.Media.DATA;
                }

                // Get column index.
                int imageColumnIndex = cursor.getColumnIndex(columnName);

                // Get column value which is the uri related file local path.
                ret = cursor.getString(imageColumnIndex);
            }
            cursor.close();
        }

        return ret;
    }

    private static String getPathFromDoc(ContentResolver contentResolver, Uri uri) {
        String path = "";
        // Get uri related document id.
        String documentId = DocumentsContract.getDocumentId(uri);

        // Get uri authority.
        String uriAuthority = uri.getAuthority();

        if ("com.android.providers.media.documents".equals(uriAuthority)) {
            String idArr[] = documentId.split(":");
            if (idArr.length == 2) {
                // First item is document type.
                String docType = idArr[0];

                // Second item is document real id.
                String realDocId = idArr[1];

                // Get content uri by document type.
                Uri mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                if ("image".equals(docType)) {
                    mediaContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(docType)) {
                    mediaContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(docType)) {
                    mediaContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                // Get where clause with real document id.
                String whereClause = MediaStore.Images.Media._ID + " = " + realDocId;

                path = getPathFromContent(contentResolver, mediaContentUri, whereClause);
            }

        } else if ("com.android.providers.downloads.documents".equals(uriAuthority)) {
            // Build download uri.
            Uri downloadUri = Uri.parse("content://downloads/public_downloads");

            // Append download document id at uri end.
            Uri downloadUriAppendId = ContentUris.withAppendedId(downloadUri, Long.valueOf(documentId));

            path = getPathFromContent(contentResolver, downloadUriAppendId, null);

        } else if ("com.android.externalstorage.documents".equals(uriAuthority)) {
            String idArr[] = documentId.split(":");
            if (idArr.length == 2) {
                String type = idArr[0];
                String realDocId = idArr[1];

                if ("primary".equalsIgnoreCase(type)) {
                    path = Environment.getExternalStorageDirectory() + "/" + realDocId;
                }
            }
        }

        return path;
    }

    //endregion
}
