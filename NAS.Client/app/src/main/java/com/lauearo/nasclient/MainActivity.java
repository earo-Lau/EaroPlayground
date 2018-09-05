package com.lauearo.nasclient;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Model.UploadingViewModel;
import com.lauearo.nasclient.Service.UploadService;

import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mUploadingRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == Constants.ACTION_REQUEST_PICK_FILE) {
            final Uri fileUri = data.getData();
            assert fileUri != null;

            try {
                UploadService.getInstance().newUpload(this.getBaseContext(), fileUri);
                onLoadUploadingList();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onLoadUploadingList() {
        UploadingAdaptor uploadingAdaptor = new UploadingAdaptor();
        mUploadingRecyclerView.setAdapter(uploadingAdaptor);
    }

    private class UploadingHolder extends RecyclerView.ViewHolder {
        private TextView mFileNameTextView;
        private ProgressBar mUploadingProgressBar;
        private ImageButton mUploadingStatusBtn;

        private View mView;
        private MenuItem.OnMenuItemClickListener mRemoveMenuItemListener;

        UploadingHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mFileNameTextView = itemView.findViewById(R.id.list_item_uploading_file_name);
            mUploadingProgressBar = itemView.findViewById(R.id.list_item_uploading_progress_bar);
            mUploadingStatusBtn = itemView.findViewById(R.id.list_item_uploading_status_btn);

            mUploadingProgressBar.setMax(100);

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
        private List<UploadingViewModel> mUploadModelList;

        UploadingAdaptor() {
            mUploadModelList = UploadService.getInstance().getUploadingViewModels();
        }

        @NonNull
        @Override
        public UploadingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
            View view = layoutInflater.inflate(R.layout.list_item_uploading, parent, false);

            return new UploadingHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UploadingHolder holder, int position) {
            UploadingViewModel uploadingVM = mUploadModelList.get(position);
            uploadingVM.addStatusUpdateEventListener((status, e) -> holder.updateStatus(status));
            uploadingVM.addProgressUpdateEventListener((progress, e) -> holder.updateProgress(progress));
            uploadingVM.addCancelEventListeners((e -> onLoadUploadingList()));

            holder.setItemClickListener((v) -> {
                int currentStatus = uploadingVM.getStatus();
                uploadingVM.setStatus(Constants.UPLOADING_STATUS_PAUSE ^ currentStatus);
            });
            holder.setRemoveMenuItemClickListener(item -> {
                UploadService.getInstance().cancelUpload(uploadingVM);

                return true;
            });
            holder.setFileNameText(uploadingVM.getUploadModel().getName());
            holder.updateStatus(uploadingVM.getStatus());
        }

        @Override
        public int getItemCount() {
            return mUploadModelList.size();
        }
    }

}
