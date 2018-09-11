package com.lauearo.nasclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Model.UploadingViewModel;
import com.lauearo.nasclient.Provider.ModelProvider.CacheViewModelProvider;
import com.lauearo.nasclient.Provider.ModelProvider.IUploadViewModelProvider;
import com.lauearo.nasclient.Service.StreamingService;
import com.lauearo.nasclient.Service.UploadService;

import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mUploadingRecyclerView;
    private IUploadViewModelProvider mViewModelProvider;
    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);

        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registerReceiver();

        mViewModelProvider = CacheViewModelProvider.getInstance();

        mUploadingRecyclerView = findViewById(R.id.uploading_recycler_view);
        mUploadingRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        onLoadUploadingList();

        FloatingActionButton uploadBtn = findViewById(R.id.uploadBtn);
        uploadBtn.setOnClickListener((View v) -> {
            Intent chooseIntent = new Intent(Intent.ACTION_GET_CONTENT);
            chooseIntent.setType("*/*");

            startActivityForResult(Intent.createChooser(chooseIntent, "Select File"),
                    Constants.ACTION_REQUEST_PICK_FILE);
        });

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_NEW);
        filter.addAction(Constants.ACTION_CANCEL);
        filter.addAction(Constants.ACTION_DONE);

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String intentAction = intent.getAction();
                Date date = new Date();
                Log.d(".BroadcastReceiver", String.format("received message %s at %s", intentAction, date.toString()));

                if (Constants.ACTION_DONE.equalsIgnoreCase(intentAction)) {
                    String id = intent.getStringExtra("id");
                    mViewModelProvider.rmViewModel(id);

                    onLoadUploadingList();
                }
            }
        };

        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == Constants.ACTION_REQUEST_PICK_FILE) {
            final Uri fileUri = data.getData();
            assert fileUri != null;
            mViewModelProvider.newUpload(getContentResolver(), fileUri);

            Intent uploadService = UploadService.newIntent(getApplicationContext());
            startService(uploadService);
            onLoadUploadingList();
        }
    }

    private void onLoadUploadingList() {
        UploadingAdaptor uploadingAdaptor = new UploadingAdaptor();
        mUploadingRecyclerView.setAdapter(uploadingAdaptor);
    }

    private class UploadingHolder extends RecyclerView.ViewHolder {
        private final TextView mFileNameTextView;
        private final ProgressBar mUploadingProgressBar;
        private final ImageButton mUploadingStatusBtn;

        private final View mView;
        private MenuItem.OnMenuItemClickListener mRemoveMenuItemListener;


        UploadingHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mFileNameTextView = itemView.findViewById(R.id.list_item_uploading_file_name);
            mUploadingProgressBar = itemView.findViewById(R.id.list_item_uploading_progress_bar);
            mUploadingStatusBtn = itemView.findViewById(R.id.list_item_uploading_status_btn);

            mUploadingProgressBar.setMax(100);
//            mUploadingProgressBar.setProgress(50);

            itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
                MenuItem removeMenuItem = menu.add(0, v.getId(), 0, "Remove");
                if (mRemoveMenuItemListener != null) {
                    removeMenuItem.setOnMenuItemClickListener(mRemoveMenuItemListener);
                }
            });
        }

        void updateStatus(int status) {
            if (status == Constants.UPLOADING_STATUS_PAUSE) {
                mUploadingStatusBtn.setImageResource(R.drawable.ic_play_arrow_black_24dp);
            } else if (status == Constants.UPLOADING_STATUS_FAILURE) {
                mUploadingStatusBtn.setImageResource(R.drawable.ic_error_outline_black_24dp);
            } else {
                mUploadingStatusBtn.setImageResource(R.drawable.ic_pause_black_24dp);
            }
        }

        void setItemClickListener(View.OnClickListener listener) {
            mView.setOnClickListener(listener);
            mUploadingStatusBtn.setOnClickListener(listener);
        }

        void updateProgress(int progress) {
            mUploadingProgressBar.setProgress(progress);
        }

        void setFileNameText(String fileName) {
            mFileNameTextView.setText(fileName);
        }

        void setRemoveMenuItemClickListener(MenuItem.OnMenuItemClickListener removeMenuItemListener) {
            mRemoveMenuItemListener = removeMenuItemListener;
        }
    }

    private class UploadingAdaptor extends RecyclerView.Adapter<UploadingHolder> {

        @NonNull
        @Override
        public UploadingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.list_item_uploading, parent, false);

            return new UploadingHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UploadingHolder holder, int position) {
            UploadingViewModel uploadingVM = mViewModelProvider.getUploadingList().get(position);
            uploadingVM.addStatusUpdateEventListener((status, e) -> holder.updateStatus(status));
            uploadingVM.addProgressUpdateEventListener((progress, e) -> holder.updateProgress(progress));

            holder.setItemClickListener((v) -> {
                int currentStatus = uploadingVM.getStatus();
                if (currentStatus == Constants.UPLOADING_STATUS_PLAY) {
                    uploadingVM.setStatus(Constants.UPLOADING_STATUS_PAUSE);
                } else {
                    uploadingVM.setStatus(Constants.UPLOADING_STATUS_PLAY);
                }
                Intent streamIntent = StreamingService.newIntent(getApplicationContext());
                streamIntent.putExtra("id", uploadingVM.getUploadModel().getId());
                startService(streamIntent);
            });
            holder.setRemoveMenuItemClickListener(item -> {
                uploadingVM.cancel();

                mViewModelProvider.rmViewModel(uploadingVM.getUploadModel().getId());
                onLoadUploadingList();

                return true;
            });
            holder.setFileNameText(uploadingVM.getUploadModel().getName());
            holder.updateStatus(uploadingVM.getStatus());
        }

        @Override
        public int getItemCount() {
            return mViewModelProvider.getUploadingList().size();
        }
    }

}
