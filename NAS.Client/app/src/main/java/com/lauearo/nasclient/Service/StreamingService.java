package com.lauearo.nasclient.Service;

import NAS.Model.UploadModelOuterClass.UploadModel;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import com.google.protobuf.ByteString;
import com.lauearo.nasclient.Model.Constants;
import com.lauearo.nasclient.Model.UploadingViewModel;
import com.lauearo.nasclient.Provider.HttpTaskProvider.IHttpTaskProvider;
import com.lauearo.nasclient.Provider.HttpTaskProvider.ITaskCallBack;
import com.lauearo.nasclient.Provider.HttpTaskProvider.PostProtoBufTask;
import com.lauearo.nasclient.Provider.ModelProvider.CacheViewModelProvider;
import com.lauearo.nasclient.Provider.ModelProvider.IStreamingNodeProvider;
import com.lauearo.nasclient.Provider.ModelProvider.IStreamingNodeProviderImpl;
import com.lauearo.nasclient.Provider.ModelProvider.IUploadViewModelProvider;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicInteger;

import static NAS.Model.UploadModelOuterClass.StreamingNode;

public class StreamingService extends IntentService {
    private static final String TAG = "StreamingService";
    private final IUploadViewModelProvider mViewModelProvider;

    public StreamingService() {
        super(TAG);
        mViewModelProvider = CacheViewModelProvider.getInstance();
    }

    public static Intent newIntent(Context context) {
        return new Intent(context, StreamingService.class);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        assert intent != null;
        String id = intent.getStringExtra("id");
        UploadingViewModel viewModel = mViewModelProvider.getUploadingViewModel(id);

        Observable.fromArray(viewModel)
                  .observeOn(Schedulers.newThread())
                  .subscribe(new Observer<UploadingViewModel>() {
                      @Override
                      public void onSubscribe(Disposable d) {

                      }

                      @Override
                      public void onNext(UploadingViewModel viewModel) {
                          if (viewModel.getStatus() != Constants.UPLOADING_STATUS_PLAY) {
                              return;
                          }
                          IStreamingNodeProvider nodeProvider =
                                  new IStreamingNodeProviderImpl(viewModel.getUploadModel().getRoot());

                          Log.i(TAG, String.format(">>>>>>>>>>>>> Earo say begin stream model: %s",
                                  viewModel.getUploadModel().getName()));

                          Flowable<StreamingNode> f = Flowable.create(emitter -> {
                              boolean flag;
                              while (nodeProvider.hasNext()) {
                                  if (emitter.isCancelled()) {
                                      break;
                                  }

                                  flag = false;
                                  while (emitter.requested() == 0) {
                                      if (!flag) {
                                          flag = true;
                                      }
                                  }

                                  StreamingNode node = nodeProvider.next();
                                  int id = node.getId();
                                  byte[] progress = viewModel.getUploadModel().getProgress().toByteArray();

                                  //skip if progress mark as done
                                  if (progress.length > 0 && progress[id] != 1) {
                                      emitter.onNext(node);
                                  }
                              }

                              emitter.onComplete();
                          }, BackpressureStrategy.BUFFER);

                          StreamWorker worker = new StreamWorker(viewModel);
                          f.subscribeOn(Schedulers.io())
                           .observeOn(AndroidSchedulers.mainThread())
                           .subscribe(worker);

                          worker.onRequired();
                      }

                      @Override
                      public void onError(Throwable e) {

                      }

                      @Override
                      public void onComplete() {

                      }
                  });
    }

    private void uploadDone(UploadingViewModel viewModel) {
        viewModel.setStatus(Constants.UPLOADING_STATUS_FINISH);
        try {
            IHttpTaskProvider<UploadModel> finishTask = new PostProtoBufTask<>(Constants.NAS_SERVER_URL + "/api" +
                    "/upload" +
                    "/done");
            finishTask.send(viewModel.getUploadModel(), new ITaskCallBack<UploadModel>() {
                @Override
                public void onSuccess(String resultString) {
                    if (resultString.equalsIgnoreCase("ok")) {
                        Intent finishIntent = new Intent(Constants.ACTION_DONE);
                        finishIntent.putExtra("id", viewModel.getUploadModel().getId());

                        sendBroadcast(finishIntent);
                    }
                }

                @Override
                public void onFailure(Exception exceptions) {
                    Intent createResult = new Intent(Constants.ACTION_FAILED);
                    sendBroadcast(createResult);
                }
            });

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    private class StreamWorker implements Subscriber<StreamingNode> {
        final UploadingViewModel mViewModel;
        Subscription mSubscription;
        int mRunning = 0;
        boolean mStopRequest = false;

        StreamWorker(UploadingViewModel viewModel) {
            mViewModel = viewModel;
        }

        void onRequired() {
            new Thread(() -> {
                synchronized (StreamWorker.this) {
                    do {
                        if (mViewModel.getStatus() != Constants.UPLOADING_STATUS_PLAY) return;

                        try {
                            if (mRunning < 10) {
                                mSubscription.request(1);
                                mRunning += ((AtomicInteger) mSubscription).get();
                            }
                            StreamWorker.this.wait(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (!mStopRequest);
                }

                synchronized (StreamWorker.this) {
                    try {
                        while (mRunning > 0) {
                            if (mViewModel.getStatus() != Constants.UPLOADING_STATUS_PLAY) {
                                Log.i(TAG, String.format(">>>>>>>>>>>>>>>> Earo say stop upload stream %s",
                                        mViewModel.getUploadModel().getName()));

                                mSubscription.cancel();
                                return;
                            }

                            Log.i(TAG, String.format(">>>>>>>>>>>>>>>> Earo say mRunning before done: %s", mRunning));

                            StreamWorker.this.wait(5000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (mViewModel.getStatus() == Constants.UPLOADING_STATUS_PLAY) {
                    uploadDone(mViewModel);
                }

            }).run();
        }

        @Override
        public void onSubscribe(Subscription s) {
            mSubscription = s;
        }

        @SuppressLint("CheckResult")
        @Override
        public void onNext(StreamingNode streamingNode) {
            if (mViewModel.getStatus() != Constants.UPLOADING_STATUS_PLAY) {
                if (mViewModel.getStatus() == Constants.UPLOADING_STATUS_CANCEL)
                    mSubscription.cancel();

                return;
            }

            Log.i(TAG, String.format(">>>>>>>>>>>>> Earo say begin to stream node: %s",
                    streamingNode.getId()));
            try {
                Uri fileUri = mViewModel.getFileUri();
                InputStream inputStream = getContentResolver().openInputStream(fileUri);
                assert inputStream != null;

                byte[] chunkStream = new byte[200000];
                int offset = streamingNode.getId() * 200000;
                //noinspection ResultOfMethodCallIgnored
                inputStream.skip(offset);
                int len = inputStream.read(chunkStream);
                ByteString byteString = ByteString.copyFrom(chunkStream, 0, len);
                String modelId = mViewModel.getUploadModel().getId();

                StreamingNode uploadStreaming = streamingNode.toBuilder()
                                                             .setLength(len)
                                                             .setStream(byteString)
                                                             .setUploadModle(modelId)
                                                             .build();

                IHttpTaskProvider<StreamingNode> streamingTask =
                        new PostProtoBufTask<>(Constants.NAS_SERVER_URL + "/api/upload/stream");

                UploadingViewModel.CancelEventListener cancelEventListener =
                        e -> streamingTask.cancelTask();
                mViewModel.addCancelEventListeners(cancelEventListener);

                streamingTask.send(uploadStreaming, new ITaskCallBack<StreamingNode>() {
                    @Override
                    public void onSuccess(String resultString) {
                        synchronized (StreamWorker.this) {

                            if (resultString.equalsIgnoreCase("ok")) {
                                mViewModel.setProgress(uploadStreaming.getLength());
                                mRunning--;
                                Log.i(TAG, String.format(">>>>>>>>>>>>> Earo say streaming success " +
                                                "%s",
                                        streamingNode.getId()));
                                // update progress by node id
                                byte[] progress = mViewModel.getUploadModel().getProgress().toByteArray();
                                progress[streamingNode.getId()] = 1;

                                UploadModel nUploadModel = mViewModel.getUploadModel()
                                                                     .toBuilder()
                                                                     .setProgress(ByteString.copyFrom(progress))
                                                                     .build();

                                mViewModel.setUploadModel(nUploadModel);
                            }
                            mViewModel.rmCancelEventListener(cancelEventListener);
                            StreamWorker.this.notify();
                        }
                    }

                    @Override
                    public void onFailure(Exception exceptions) {
                        synchronized (StreamWorker.this) {
                            mViewModel.setStatus(Constants.UPLOADING_STATUS_FAILURE);
                            mRunning--;
                            mStopRequest = true;
                            mSubscription.cancel();

                            mViewModel.rmCancelEventListener(cancelEventListener);
                            StreamWorker.this.notify();
                        }
                    }

                    @Override
                    public void onCancel() {
                        synchronized (StreamWorker.this) {
                            mViewModel.rmCancelEventListener(cancelEventListener);

                            mStopRequest = true;
                            mSubscription.cancel();

                            StreamWorker.this.notify();
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();

                mStopRequest = true;
                mSubscription.cancel();
            }
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "Uploading stream error", e);
            mStopRequest = true;
            mSubscription.cancel();
        }

        @Override
        public void onComplete() {
            Log.i(TAG, String.format(">>>>>>>>>>> Earo say %s complete streaming",
                    mViewModel.getUploadModel().getName()));
            mStopRequest = true;
        }
    }
}

