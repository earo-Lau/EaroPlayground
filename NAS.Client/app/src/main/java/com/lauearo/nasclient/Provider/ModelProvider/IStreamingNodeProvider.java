package com.lauearo.nasclient.Provider.ModelProvider;

import NAS.Model.UploadModelOuterClass.StreamingNode;

import java.util.Iterator;

public interface IStreamingNodeProvider extends Iterator<StreamingNode> {
    long size();

    void reset();
}
