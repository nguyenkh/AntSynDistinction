package io.sentence;

import java.io.IOException;

import tree.Tree;

public interface TreeInputStream {
    // return null if end of file
    public Tree readTree() throws IOException;
    public long getReadLine();
    public void close() throws IOException;
}
