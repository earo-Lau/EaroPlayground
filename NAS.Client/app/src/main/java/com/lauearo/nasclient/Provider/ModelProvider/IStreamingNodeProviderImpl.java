package com.lauearo.nasclient.Provider.ModelProvider;

import NAS.Model.UploadModelOuterClass.StreamingNode;

import java.util.LinkedList;
import java.util.Queue;

public class IStreamingNodeProviderImpl implements IStreamingNodeProvider {
    private Queue<StreamingNode> iterator;
    private StreamingNode mRoot;

    public IStreamingNodeProviderImpl(StreamingNode root) {
        this.mRoot = root;
        reset();
    }


    @Override
    public void reset() {
        iterator = new LinkedList<>();

        push(mRoot);
    }


    private void push(StreamingNode root) {
        while (root != null) {
            iterator.add(root);
            root = root.hasLeft() ? root.getLeft() : null;
        }
    }

    @Override
    public boolean hasNext() {
        return !iterator.isEmpty();
    }

    @Override
    public StreamingNode next() {
        StreamingNode root = iterator.poll();
        if (root != null && root.hasRight()) push(root.getRight());

        return root;
    }

}
