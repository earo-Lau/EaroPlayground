package com.lauearo.nasclient.Provider.ModelProvider;

import NAS.Model.UploadModelOuterClass.StreamingNode;

import java.util.LinkedList;
import java.util.Queue;

public class IStreamingNodeProviderImpl implements IStreamingNodeProvider {
    private Queue<StreamingNode> iterator;
    private final StreamingNode mRoot;

    public IStreamingNodeProviderImpl(StreamingNode root) {
        this.mRoot = root;
        reset();
    }


    @Override
    public long size() {
        if (mRoot == null) {
            return 0;
        }

        StreamingNode cur = mRoot;
        while (cur.hasRight()) {
            cur = cur.getRight();
        }

        return cur.getId() + 1;
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
